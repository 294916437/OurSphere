package com.theoyu.oursphere.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.alibaba.nacos.shaded.com.google.common.collect.Sets;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.theoyu.framework.common.exception.BusinessException;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.framework.context.holder.LoginUserContextHolder;
import com.theoyu.oursphere.note.biz.constants.MQConstants;
import com.theoyu.oursphere.note.biz.constants.RedisKeyConstants;
import com.theoyu.oursphere.note.biz.enums.NoteStatusEnum;
import com.theoyu.oursphere.note.biz.enums.NoteTypeEnum;
import com.theoyu.oursphere.note.biz.enums.NoteVisibleEnum;
import com.theoyu.oursphere.note.biz.enums.ResponseCodeEnum;
import com.theoyu.oursphere.note.biz.model.entity.ChannelPO;
import com.theoyu.oursphere.note.biz.model.entity.NotePO;
import com.theoyu.oursphere.note.biz.model.entity.TopicPO;
import com.theoyu.oursphere.note.biz.model.mapper.ChannelPOMapper;
import com.theoyu.oursphere.note.biz.model.mapper.NotePOMapper;
import com.theoyu.oursphere.note.biz.model.mapper.TopicPOMapper;
import com.theoyu.oursphere.note.biz.model.vo.FindNoteDetailReqVO;
import com.theoyu.oursphere.note.biz.model.vo.FindNoteDetailRspVO;
import com.theoyu.oursphere.note.biz.model.vo.PublishNoteReqVO;
import com.theoyu.oursphere.note.biz.model.vo.UpdateNoteReqVO;
import com.theoyu.oursphere.note.biz.rpc.IdGeneratorRpcService;
import com.theoyu.oursphere.note.biz.rpc.KVRpcService;
import com.theoyu.oursphere.note.biz.rpc.UserRpcService;
import com.theoyu.oursphere.note.biz.service.NoteService;
import com.theoyu.oursphere.user.dto.response.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NoteServiceImpl implements NoteService {
    @Resource
    private NotePOMapper notePOMapper;
    @Resource
    private TopicPOMapper topicPOMapper;
    @Resource
    private IdGeneratorRpcService idGeneratorRpcService;
    @Resource
    private KVRpcService keyValueRpcService;
    @Resource
    private UserRpcService userRpcService;
    @Resource
    private ChannelPOMapper channelPOMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(1, TimeUnit.HOURS) // 设置缓存条目在写入后 1 小时过期
            .build();


    /**
     * 笔记发布
     *
     * @param publishNoteReqVO
     * @return
     */
    @Override
    public Response<?> publishNote(PublishNoteReqVO publishNoteReqVO) {
        // 笔记类型
        Integer type = publishNoteReqVO.getType();

        // 获取对应类型的枚举
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        // 若非图文、视频，抛出业务业务异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BusinessException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        // 笔记内容是否为空，默认值为 true，即空
        Boolean isContentEmpty = true;
        String videoUri = null;
        switch (noteTypeEnum) {
            case IMAGE_TEXT: // 图文笔记
                List<String> imgUriList = publishNoteReqVO.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");
                // 将图片链接拼接，以逗号分隔
                imgUris = StringUtils.join(imgUriList, ",");

                break;
            case VIDEO: // 视频笔记
                videoUri = publishNoteReqVO.getVideoUri();
                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }
        // 判断所选频道是否存在
        Long channelId = publishNoteReqVO.getChannelId();
        ChannelPO channelPO = channelPOMapper.selectByPrimaryKey(channelId);

        if (Objects.isNull(channelPO)) {
            throw new BusinessException(ResponseCodeEnum.CHANNEL_NOT_FOUND);
        }
        // RPC: 调用分布式 ID 生成服务，生成笔记 ID
        String snowflakeIdId = idGeneratorRpcService.getSnowflakeId();
        // 生成笔记内容 UUID
        String contentUuid =null;;

        // 笔记内容
        String content = publishNoteReqVO.getContent();

        // 若用户填写了笔记内容
        if (StringUtils.isNotBlank(content)) {
            // 内容是否为空，置为 false，即不为空
            isContentEmpty = false;
            contentUuid = UUID.randomUUID().toString();;
            // RPC: 调用 KV 键值服务，存储短文本
            boolean isSavedSuccess = keyValueRpcService.saveNoteContent(contentUuid, content);

            // 若存储失败，抛出业务异常，提示用户发布笔记失败
            if (!isSavedSuccess) {
                throw new BusinessException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
            }
        }

        // 话题处理
        String topicIds = handleTopics(publishNoteReqVO.getTopics());

        // 发布者用户 ID
        Long creatorId = LoginUserContextHolder.getUserId();

        // 构建笔记 PO 对象
        NotePO notePO = NotePO.builder()
                .id(Long.valueOf(snowflakeIdId))
                .isContentEmpty(isContentEmpty)
                .creatorId(creatorId)
                .imgUris(imgUris)
                .title(publishNoteReqVO.getTitle())
                .type(type)
                .channelId(publishNoteReqVO.getChannelId())
                .topicIds(topicIds)
                .visible(NoteVisibleEnum.PUBLIC.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status(NoteStatusEnum.NORMAL.getCode())
                .isTop(Boolean.FALSE)
                .videoUri(videoUri)
                .contentUuid(contentUuid)
                .build();

        try {
            // 笔记入库存储
            notePOMapper.insert(notePO);
        } catch (Exception e) {
            log.error("==> 笔记存储失败", e);
            // RPC: 笔记保存失败，则删除笔记内容
            if (StringUtils.isNotBlank(contentUuid)) {
                keyValueRpcService.deleteNoteContent(contentUuid);
            }
        }

        return Response.success();
    }

    /**
     * 笔记详情
     *
     * @param findNoteDetailReqVO
     * @return
     */
    @Override
    @SneakyThrows
    public Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO) {
        // 查询的笔记 ID
        Long noteId = findNoteDetailReqVO.getId();

        // 当前登录用户
        Long userId = LoginUserContextHolder.getUserId();
        // 先从本地缓存中查询
        String findNoteDetailRspVOStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        if (StringUtils.isNotBlank(findNoteDetailRspVOStrLocalCache)) {
            FindNoteDetailRspVO findNoteDetailRspVO = JsonUtils.parseObject(findNoteDetailRspVOStrLocalCache, FindNoteDetailRspVO.class);
            log.info("==> 命中本地缓存；{}", findNoteDetailRspVOStrLocalCache);
            // 可见性校验
            checkNoteVisibleFromVO(userId, findNoteDetailRspVO);
            return Response.success(findNoteDetailRspVO);
        }
        // 从 Redis 缓存中获取
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        String noteDetailJson = redisTemplate.opsForValue().get(noteDetailRedisKey);

        // 若Redis缓存中有该笔记的数据，则直接返回
        if (StringUtils.isNotBlank(noteDetailJson)) {
            FindNoteDetailRspVO findNoteDetailRspVO = JsonUtils.parseObject(noteDetailJson, FindNoteDetailRspVO.class);
            // 写入本地缓存
            threadPoolTaskExecutor.submit(() -> {
                LOCAL_CACHE.put(noteId,
                        Objects.isNull(findNoteDetailRspVO) ? "null" : JsonUtils.toJsonString(findNoteDetailRspVO));
            });
            // 可见性校验
            checkNoteVisibleFromVO(userId, findNoteDetailRspVO);

            return Response.success(findNoteDetailRspVO);
        }
        // 若Redis不存在，则从数据库查询笔记
        NotePO notePO = notePOMapper.selectByPrimaryKey(noteId);
        // 若该笔记不存在，则抛出业务异常
        if (Objects.isNull(notePO)) {
            threadPoolTaskExecutor.execute(() -> {
                // 防止缓存穿透，将空数据存入 Redis 缓存 (过期时间不宜设置过长)
                // 保底1分钟 + 随机秒数
                long expireSeconds = 60 + RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(noteDetailRedisKey, "null", expireSeconds, TimeUnit.SECONDS);
            });
            throw new BusinessException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }


        // 可见性校验
        Integer visible = notePO.getVisible();
        checkNoteVisible(visible, userId, notePO.getCreatorId());
        //并发调用用户服务和KV服务
        // RPC: 调用用户服务
        Long creatorId = notePO.getCreatorId();
        CompletableFuture<FindUserByIdRspDTO> userResultFuture = CompletableFuture
                .supplyAsync(() -> userRpcService.findById(creatorId), threadPoolTaskExecutor);
        // RPC: 调用 K-V 存储服务获取内容
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if (Objects.equals(notePO.getIsContentEmpty(), Boolean.FALSE)) {
            contentResultFuture = CompletableFuture
                    .supplyAsync(() -> keyValueRpcService.findNoteContent(notePO.getContentUuid()), threadPoolTaskExecutor);
        }

        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<FindNoteDetailRspVO> resultFuture = CompletableFuture
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s -> {
                    // 获取 Future 返回的结果
                    FindUserByIdRspDTO user = userResultFuture.join();
                    String content = finalContentResultFuture.join();

                    // 笔记类型
                    Integer noteType = notePO.getType();
                    // 图文笔记图片链接(字符串)
                    String imgUrisStr = notePO.getImgUris();
                    // 图文笔记图片链接(集合)
                    List<String> imgUris = null;
                    // 如果查询的是图文笔记，需要将图片链接的逗号分隔开，转换成集合
                    if (Objects.equals(noteType, NoteTypeEnum.IMAGE_TEXT.getCode())
                            && StringUtils.isNotBlank(imgUrisStr)) {
                        imgUris = List.of(imgUrisStr.split(","));
                    }

                    // 构建返参 VO 实体类
                    return FindNoteDetailRspVO.builder()
                            .id(notePO.getId())
                            .type(notePO.getType())
                            .title(notePO.getTitle())
                            .content(content)
                            .imgUris(imgUris)
                            .topicId(notePO.getTopicId())
                            .topicName(notePO.getTopicName())
                            .creatorId(notePO.getCreatorId())
                            .creatorName(user.getNickName())
                            .avatar(user.getAvatar())
                            .videoUri(notePO.getVideoUri())
                            .updateTime(notePO.getUpdateTime())
                            .visible(notePO.getVisible())
                            .build();

                });
        FindNoteDetailRspVO findNoteDetailRspVO = resultFuture.get();
        // 异步线程中将笔记详情存入 Redis
        threadPoolTaskExecutor.submit(() -> {
            String noteDetailJson1 = JsonUtils.toJsonString(findNoteDetailRspVO);
            // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            redisTemplate.opsForValue().set(noteDetailRedisKey, noteDetailJson1, expireSeconds, TimeUnit.SECONDS);
        });

        return Response.success(findNoteDetailRspVO);
    }

    @Override
    public Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO) {
        // 笔记 ID
        Long noteId = updateNoteReqVO.getId();
        // 笔记类型
        Integer type = updateNoteReqVO.getType();

        // 获取对应类型的枚举
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        // 若非图文、视频，抛出业务业务异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BusinessException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        String videoUri = null;
        switch (noteTypeEnum) {
            case IMAGE_TEXT: // 图文笔记
                List<String> imgUriList = updateNoteReqVO.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");

                imgUris = StringUtils.join(imgUriList, ",");
                break;
            case VIDEO: // 视频笔记
                videoUri = updateNoteReqVO.getVideoUri();
                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();
        NotePO selectNotePO = notePOMapper.selectByPrimaryKey(noteId);

        // 笔记不存在
        if (Objects.isNull(selectNotePO)) {
            throw new BusinessException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许更新笔记
        if (!Objects.equals(currUserId, selectNotePO.getCreatorId())) {
            throw new BusinessException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 话题
        Long topicId = updateNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            topicName = topicPOMapper.selectNameByPrimaryKey(topicId);

            // 判断一下提交的话题, 是否是真实存在的
            if (StringUtils.isBlank(topicName)) throw new BusinessException(ResponseCodeEnum.TOPIC_NOT_FOUND);
        }
        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);

        // 更新笔记元数据表 t_note
        String content = updateNoteReqVO.getContent();
        NotePO notePO = NotePO.builder()
                .id(noteId)
                .isContentEmpty(StringUtils.isBlank(content))
                .imgUris(imgUris)
                .title(updateNoteReqVO.getTitle())
                .topicId(updateNoteReqVO.getTopicId())
                .topicName(topicName)
                .type(type)
                .updateTime(LocalDateTime.now())
                .videoUri(videoUri)
                .build();

        notePOMapper.updateByPrimaryKeySelective(notePO);
        // 一致性保证：延迟双删
        Message<String> message = MessageBuilder.withPayload(String.valueOf(noteId))
                .build();

        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, message,
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("## 延时删除 Redis 笔记缓存消息发送成功...");
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("## 延时删除 Redis 笔记缓存消息发送失败...", e);
                    }
                },
                3000, // 超时时间(毫秒)
                1 // 延迟级别，1 表示延时 1s
        );

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> RocketMQ：删除笔记本地缓存发送成功...");
        // 笔记内容更新
        // 查询此篇笔记内容对应的 UUID
        NotePO notePO1 = notePOMapper.selectByPrimaryKey(noteId);
        String contentUuid = notePO1.getContentUuid();

        // 笔记内容是否更新成功
        boolean isUpdateContentSuccess = false;
        if (StringUtils.isBlank(content)) {
            // 若笔记内容为空，则删除 K-V 存储
            isUpdateContentSuccess = keyValueRpcService.deleteNoteContent(contentUuid);
        } else {
            contentUuid = StringUtils.isBlank(contentUuid) ? UUID.randomUUID().toString() : contentUuid;
            // 调用 K-V 更新短文本
            isUpdateContentSuccess = keyValueRpcService.saveNoteContent(contentUuid, content);
        }

        // 如果更新失败，抛出业务异常，回滚事务
        if (!isUpdateContentSuccess) {
            throw new BusinessException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }

        return Response.success();
    }
    /**
     * 删除本地笔记缓存
     * @param noteId
     */
    @Override
    public void deleteNoteLocalCache(Long noteId) {
        LOCAL_CACHE.invalidate(noteId);
    }

    /**
     * 校验笔记的可见性（针对 VO 实体类）
     * @param userId
     * @param findNoteDetailRspVO
     */
    private void checkNoteVisibleFromVO(Long userId, FindNoteDetailRspVO findNoteDetailRspVO) {
        if (Objects.nonNull(findNoteDetailRspVO)) {
            Integer visible = findNoteDetailRspVO.getVisible();
            checkNoteVisible(visible, userId, findNoteDetailRspVO.getCreatorId());
        }
    }

    /**
     * 校验笔记的可见性
     * @param visible 是否可见
     * @param currUserId 当前用户 ID
     * @param creatorId 笔记创建者
     */
    private void checkNoteVisible(Integer visible, Long currUserId, Long creatorId) {
        if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                && !Objects.equals(currUserId, creatorId)) { // 仅自己可见, 并且访问用户为笔记创建者才能访问，非本人则抛出异常
            throw new BusinessException(ResponseCodeEnum.NOTE_PRIVATE);
        }
    }
    private String handleTopics(List<Object> topicInputs) {
        if (CollUtil.isEmpty(topicInputs)) return null;

        // 1. 分离已存在话题（ID）和新话题（名称）
        List<Long> existingTopicIds = Lists.newArrayList();
        List<String> newTopicNames = Lists.newArrayList();

        topicInputs.forEach(input -> {
            if (input instanceof Number) {
                // 已存在话题 ID
                existingTopicIds.add(Long.valueOf(String.valueOf(input)));
            } else if (input instanceof String) {
                // 新话题名称
                newTopicNames.add((String) input);
            }
        });

        // 2. 查询现有话题信息 - 批量查询
        Set<Long> existingTopicIdsSet = Sets.newHashSet();
        if (CollUtil.isNotEmpty(existingTopicIds)) {
            List<TopicPO> existingTopicPOS = topicPOMapper.selectByTopicIdIn(existingTopicIds);
            existingTopicIdsSet = existingTopicPOS.stream()
                    .map(TopicPO::getId)
                    .collect(Collectors.toSet());
        }


        // 3. 处理新标签
        List<TopicPO> newTopics = Lists.newArrayList();
        for (String topicName : newTopicNames) {
            TopicPO existingTopic = topicPOMapper.selectByTopicName(topicName);
            if (Objects.isNull(existingTopic)) {
                // 话题不存在，插入新话题
                newTopics.add(TopicPO.builder().name(topicName).build());
            } else {
                // 话题已经存在，加入现有话题 ID 列表
                existingTopicIdsSet.add(existingTopic.getId());
            }
        }

        // 4. 批量保存新话题（如果有）
        if (CollUtil.isNotEmpty(newTopics)) {
            topicPOMapper.batchInsert(newTopics);
        }

        // 5. 获取所有话题的 ID（已存在和新插入的）
        List<Long> allTopicIds = new ArrayList<>(existingTopicIdsSet);
        if (CollUtil.isNotEmpty(newTopics)) {
            newTopics.forEach(newTopic -> allTopicIds.add(newTopic.getId()));
        }

        // 6. 将所有的话题 ID 以逗号拼接
        return StringUtils.join(allTopicIds, ",");
    }
}
