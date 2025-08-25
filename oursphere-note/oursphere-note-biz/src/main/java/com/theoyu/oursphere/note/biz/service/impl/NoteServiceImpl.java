package com.theoyu.oursphere.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.alibaba.nacos.shaded.com.google.common.collect.Sets;
import com.theoyu.framework.common.exception.BusinessException;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.context.holder.LoginUserContextHolder;
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
import com.theoyu.oursphere.note.biz.rpc.IdGeneratorRpcService;
import com.theoyu.oursphere.note.biz.rpc.KVRpcService;
import com.theoyu.oursphere.note.biz.rpc.UserRpcService;
import com.theoyu.oursphere.note.biz.service.NoteService;
import com.theoyu.oursphere.user.dto.response.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
        // 笔记内容 UUID
        String contentUuid = null;

        // 笔记内容
        String content = publishNoteReqVO.getContent();

        // 若用户填写了笔记内容
        if (StringUtils.isNotBlank(content)) {
            // 内容是否为空，置为 false，即不为空
            isContentEmpty = false;
            // 生成笔记内容 UUID
            contentUuid = UUID.randomUUID().toString();
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
    public Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO) {
        // 查询的笔记 ID
        Long noteId = findNoteDetailReqVO.getId();

        // 当前登录用户
        Long userId = LoginUserContextHolder.getUserId();

        // 查询笔记
        NotePO notePO = notePOMapper.selectByPrimaryKey(noteId);

        // 若该笔记不存在，则抛出业务异常
        if (Objects.isNull(notePO)) {
            throw new BusinessException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 可见性校验
        Integer visible = notePO.getVisible();
        checkNoteVisible(visible, userId, notePO.getCreatorId());

        // RPC: 调用用户服务
        Long creatorId = notePO.getCreatorId();
        FindUserByIdRspDTO findUserByIdRspDTO = userRpcService.findById(creatorId);

        // RPC: 调用 K-V 存储服务获取内容
        String content = null;
        if (Objects.equals(notePO.getIsContentEmpty(), Boolean.FALSE)) {
            content = keyValueRpcService.findNoteContent(notePO.getContentUuid());
        }

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
        FindNoteDetailRspVO findNoteDetailRspVO = FindNoteDetailRspVO.builder()
                .id(notePO.getId())
                .type(notePO.getType())
                .title(notePO.getTitle())
                .content(content)
                .imgUris(imgUris)
                .topicId(notePO.getTopicId())
                .topicName(notePO.getTopicName())
                .creatorId(notePO.getCreatorId())
                .creatorName(findUserByIdRspDTO.getNickName())
                .avatar(findUserByIdRspDTO.getAvatar())
                .videoUri(notePO.getVideoUri())
                .updateTime(notePO.getUpdateTime())
                .visible(notePO.getVisible())
                .build();

        return Response.success(findNoteDetailRspVO);
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
