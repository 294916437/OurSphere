package com.theoyu.oursphere.comment.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.theoyu.framework.common.constants.DateConstants;
import com.theoyu.framework.common.response.PageResponse;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.common.utils.DateUtils;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.framework.context.holder.LoginUserContextHolder;
import com.theoyu.oursphere.comment.biz.constants.MQConstants;
import com.theoyu.oursphere.comment.biz.model.dto.PublishCommentMqDTO;
import com.theoyu.oursphere.comment.biz.model.entity.CommentPO;
import com.theoyu.oursphere.comment.biz.model.mapper.CommentPOMapper;
import com.theoyu.oursphere.comment.biz.model.mapper.NoteCountPOMapper;
import com.theoyu.oursphere.comment.biz.model.vo.FindCommentItemRspVO;
import com.theoyu.oursphere.comment.biz.model.vo.FindCommentPageListReqVO;
import com.theoyu.oursphere.comment.biz.model.vo.PublishCommentReqVO;
import com.theoyu.oursphere.comment.biz.retry.SendMqRetryHelper;
import com.theoyu.oursphere.comment.biz.rpc.IdGeneratorRpcService;
import com.theoyu.oursphere.comment.biz.rpc.KeyValueRpcService;
import com.theoyu.oursphere.comment.biz.rpc.UserRpcService;
import com.theoyu.oursphere.comment.biz.service.CommentService;
import com.theoyu.oursphere.kv.dto.request.FindCommentContentReqDTO;
import com.theoyu.oursphere.kv.dto.response.FindCommentContentRspDTO;
import com.theoyu.oursphere.user.dto.response.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Resource
    private SendMqRetryHelper sendMqRetryHelper;
    @Resource
    private IdGeneratorRpcService idGeneratorRpcService;
    @Resource
    private CommentPOMapper commentPOMapper;
    @Resource
    private NoteCountPOMapper noteCountPOMapper;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Resource
    private UserRpcService userRpcService;

    /**
     * 发布评论
     *
     * @param publishCommentReqVO
     * @return
     */
    @Override
    public Response<?> publishComment(PublishCommentReqVO publishCommentReqVO) {
        // 评论正文
        String content = publishCommentReqVO.getContent();
        // 附近图片
        String imageUrl = publishCommentReqVO.getImageUrl();

        // 评论内容和图片不能同时为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl),
                "评论正文和图片不能同时为空");

        // 发布者ID
        Long creatorId = LoginUserContextHolder.getUserId();

        // 调用分布式 ID 生成服务，生成评论 ID
        String commentId = idGeneratorRpcService.generateCommentId();

        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .commentId(Long.valueOf(commentId))
                .noteId(publishCommentReqVO.getNoteId())
                .content(content)
                .imageUrl(imageUrl)
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();
        // 使用重启工具发送MQ
        sendMqRetryHelper.asyncSend(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));

        return Response.success();

    }

    @Override
    public PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO) {
        // 笔记 ID
        Long noteId = findCommentPageListReqVO.getNoteId();
        // 当前页码
        Integer pageNo = findCommentPageListReqVO.getPageNo();
        // 每页展示一级评论数
        long pageSize = 10;

        // TOPO: 先从缓存中查（后续小节补充）

        // 查询评论总数 (从 t_note_count 笔记计数表查，提升查询性能, 避免 count(*))
        Long count = noteCountPOMapper.selectCommentTotalByNoteId(noteId);

        if (Objects.isNull(count)) {
            return PageResponse.success(null, pageNo, pageSize);
        }

        // 分页返参
        List<FindCommentItemRspVO> commentRspVOS = null;

        // 若评论总数大于 0
        if (count > 0) {
            commentRspVOS = Lists.newArrayList();

            // 计算分页查询的偏移量 offset
            long offset = PageResponse.getOffset(pageNo, pageSize);

            // 查询一级评论
            List<CommentPO> oneLevelCommentPOS = commentPOMapper.selectPageList(noteId, offset, pageSize);

            // 过滤出所有最早回复的二级评论 ID
            List<Long> twoLevelCommentIds = oneLevelCommentPOS.stream()
                    .map(CommentPO::getFirstReplyCommentId)
                    .filter(firstReplyCommentId -> firstReplyCommentId != 0)
                    .toList();

            // 查询二级评论
            Map<Long, CommentPO> commentIdAndPOMap = null;
            List<CommentPO> twoLevelCommonPOS = null;
            if (CollUtil.isNotEmpty(twoLevelCommentIds)) {
                twoLevelCommonPOS = commentPOMapper.selectTwoLevelCommentByIds(twoLevelCommentIds);

                // 转 Map 集合，方便后续拼装数据
                commentIdAndPOMap = twoLevelCommonPOS.stream()
                        .collect(Collectors.toMap(CommentPO::getId, commentPO -> commentPO));
            }

            // 调用 KV 服务需要的入参
            List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
            // 调用用户服务的入参
            List<Long> userIds = Lists.newArrayList();

            // 将一级评论和二级评论合并到一起
            List<CommentPO> allCommentPOS = Lists.newArrayList();
            CollUtil.addAll(allCommentPOS, oneLevelCommentPOS);
            CollUtil.addAll(allCommentPOS, twoLevelCommonPOS);

            // 循环提取 RPC 调用需要的入参数据
            allCommentPOS.forEach(commentPO -> {
                // 构建调用 KV 服务批量查询评论内容的入参
                boolean isContentEmpty = commentPO.getIsContentEmpty();
                if (!isContentEmpty) {
                    FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                            .contentId(commentPO.getContentUuid())
                            .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(commentPO.getCreateTime()))
                            .build();
                    findCommentContentReqDTOS.add(findCommentContentReqDTO);
                }

                // 构建调用用户服务批量查询用户信息的入参
                userIds.add(commentPO.getUserId());
            });

            // RPC: 调用 KV 服务，批量获取评论内容
            List<FindCommentContentRspDTO> findCommentContentRspDTOS =
                    keyValueRpcService.batchFindCommentContent(noteId, findCommentContentReqDTOS);

            // DTO 集合转 Map, 方便后续拼装数据
            Map<String, String> commentUuidAndContentMap = null;
            if (CollUtil.isNotEmpty(findCommentContentRspDTOS)) {
                commentUuidAndContentMap = findCommentContentRspDTOS.stream()
                        .collect(Collectors.toMap(FindCommentContentRspDTO::getContentId, FindCommentContentRspDTO::getContent));
            }

            // RPC: 调用用户服务，批量获取用户信息（头像、昵称等）
            List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);

            // DTO 集合转 Map, 方便后续拼装数据
            Map<Long, FindUserByIdRspDTO> userIdAndDTOMap = null;
            if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
                userIdAndDTOMap = findUserByIdRspDTOS.stream()
                        .collect(Collectors.toMap(FindUserByIdRspDTO::getId, dto -> dto));
            }
            // PO 转 VO, 组合拼装一二级评论数据
            for (CommentPO commentPO : oneLevelCommentPOS) {
                // 一级评论
                Long userId = commentPO.getUserId();
                FindCommentItemRspVO oneLevelCommentRspVO = FindCommentItemRspVO.builder()
                        .userId(userId)
                        .commentId(commentPO.getId())
                        .imageUrl(commentPO.getImageUrl())
                        .createTime(DateUtils.formatRelativeTime(commentPO.getCreateTime()))
                        .likeTotal(commentPO.getLikeTotal())
                        .childCommentTotal(commentPO.getChildCommentTotal())
                        .build();

                // 用户信息
                setUserInfo(commentIdAndPOMap, userIdAndDTOMap, userId, oneLevelCommentRspVO);
                // 笔记内容
                setCommentContent(commentUuidAndContentMap, commentPO, oneLevelCommentRspVO);


                // 二级评论
                Long firstReplyCommentId = commentPO.getFirstReplyCommentId();
                if (CollUtil.isNotEmpty(commentIdAndPOMap)) {
                    CommentPO firstReplyCommentPO = commentIdAndPOMap.get(firstReplyCommentId);
                    if (Objects.nonNull(firstReplyCommentPO)) {
                        Long firstReplyCommentUserId = firstReplyCommentPO.getUserId();
                        FindCommentItemRspVO firstReplyCommentRspVO = FindCommentItemRspVO.builder()
                                .userId(firstReplyCommentPO.getUserId())
                                .commentId(firstReplyCommentPO.getId())
                                .imageUrl(firstReplyCommentPO.getImageUrl())
                                .createTime(DateUtils.formatRelativeTime(firstReplyCommentPO.getCreateTime()))
                                .likeTotal(firstReplyCommentPO.getLikeTotal())
                                .build();

                        setUserInfo(commentIdAndPOMap, userIdAndDTOMap, firstReplyCommentUserId, firstReplyCommentRspVO);

                        // 用户信息
                        oneLevelCommentRspVO.setFirstReplyComment(firstReplyCommentRspVO);
                        // 笔记内容
                        setCommentContent(commentUuidAndContentMap, firstReplyCommentPO, firstReplyCommentRspVO);
                    }
                }
                commentRspVOS.add(oneLevelCommentRspVO);
            }

        }

        return PageResponse.success(commentRspVOS, pageNo, count, pageSize);
    }
    /**
     * 设置评论内容
     * @param commentUuidAndContentMap
     * @param commentPO1
     * @param firstReplyCommentRspVO
     */
    private static void setCommentContent(Map<String, String> commentUuidAndContentMap, CommentPO commentPO1, FindCommentItemRspVO firstReplyCommentRspVO) {
        if (CollUtil.isNotEmpty(commentUuidAndContentMap)) {
            String contentUuid = commentPO1.getContentUuid();
            if (StringUtils.isNotBlank(contentUuid)) {
                firstReplyCommentRspVO.setContent(commentUuidAndContentMap.get(contentUuid));
            }
        }
    }

    /**
     * 设置用户信息
     * @param commentIdAndPOMap
     * @param userIdAndDTOMap
     * @param userId
     * @param oneLevelCommentRspVO
     */
    private static void setUserInfo(Map<Long, CommentPO> commentIdAndPOMap, Map<Long, FindUserByIdRspDTO> userIdAndDTOMap, Long userId, FindCommentItemRspVO oneLevelCommentRspVO) {
        FindUserByIdRspDTO findUserByIdRspDTO = userIdAndDTOMap.get(userId);
        if (Objects.nonNull(findUserByIdRspDTO)) {
            oneLevelCommentRspVO.setAvatar(findUserByIdRspDTO.getAvatar());
            oneLevelCommentRspVO.setNickname(findUserByIdRspDTO.getNickName());
        }
    }
}
