package com.theoyu.oursphere.kv.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.kv.biz.model.entity.CommentContentPO;
import com.theoyu.oursphere.kv.biz.model.entity.CommentContentPrimaryKey;
import com.theoyu.oursphere.kv.biz.model.repository.CommentContentRepository;
import com.theoyu.oursphere.kv.biz.service.CommentContentService;
import com.theoyu.oursphere.kv.dto.request.BatchAddCommentContentReqDTO;
import com.theoyu.oursphere.kv.dto.request.BatchFindCommentContentReqDTO;
import com.theoyu.oursphere.kv.dto.request.CommentContentReqDTO;
import com.theoyu.oursphere.kv.dto.request.FindCommentContentReqDTO;
import com.theoyu.oursphere.kv.dto.response.FindCommentContentRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentContentServiceImpl implements CommentContentService {

    @Resource
    private CassandraTemplate cassandraTemplate;
    @Resource
    private CommentContentRepository commentContentRepository;

    /**
     * 批量添加评论内容
     *
     * @param batchAddCommentContentReqDTO
     * @return
     */
    @Override
    public Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO) {
        List<CommentContentReqDTO> comments = batchAddCommentContentReqDTO.getComments();

        // DTO 转 PO
        List<CommentContentPO> contentPOS = comments.stream()
                .map(commentContentReqDTO -> {
                    // 构建主键类
                    CommentContentPrimaryKey commentContentPrimaryKey = CommentContentPrimaryKey.builder()
                            .noteId(commentContentReqDTO.getNoteId())
                            .yearMonth(commentContentReqDTO.getYearMonth())
                            .contentId(UUID.fromString(commentContentReqDTO.getContentId()))
                            .build();

                    // PO 实体类
                    CommentContentPO commentContentPO = CommentContentPO.builder()
                            .primaryKey(commentContentPrimaryKey)
                            .content(commentContentReqDTO.getContent())
                            .build();

                    return commentContentPO;
                }).toList();

        // 批量插入
        cassandraTemplate.batchOps()
                .insert(contentPOS)
                .execute();

        return Response.success();
    }

    @Override
    public Response<?> batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO) {
        // 归属的笔记ID
        Long noteId = batchFindCommentContentReqDTO.getNoteId();
        // 查询评论的发布年月、内容 UUID
        List<FindCommentContentReqDTO> commentContentKeys = batchFindCommentContentReqDTO.getCommentContentKeys();

        // 过滤出年月
        List<String> yearMonths = commentContentKeys.stream()
                .map(FindCommentContentReqDTO::getYearMonth)
                .distinct() // 去重
                .collect(Collectors.toList());

        // 过滤出评论内容 UUID
        List<UUID> contentIds = commentContentKeys.stream()
                .map(commentContentKey -> UUID.fromString(commentContentKey.getContentId()))
                .distinct() // 去重
                .collect(Collectors.toList());

        // 批量查询 Cassandra
        List<CommentContentPO> commentContentDOS = commentContentRepository
                .findByPrimaryKeyNoteIdAndPrimaryKeyYearMonthInAndPrimaryKeyContentIdIn(noteId, yearMonths, contentIds);

        // DO 转 DTO
        List<FindCommentContentRspDTO> findCommentContentRspDTOS = Lists.newArrayList();
        if (CollUtil.isNotEmpty(commentContentDOS)) {
            findCommentContentRspDTOS = commentContentDOS.stream()
                    .map(commentContentDO -> FindCommentContentRspDTO.builder()
                            .contentId(String.valueOf(commentContentDO.getPrimaryKey().getContentId()))
                            .content(commentContentDO.getContent())
                            .build())
                    .toList();
        }

        return Response.success(findCommentContentRspDTOS);
    }
}
