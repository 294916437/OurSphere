package com.theoyu.oursphere.kv.biz.service.impl;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.kv.biz.model.entity.NoteContentPO;
import com.theoyu.oursphere.kv.biz.model.repository.NoteContentRepository;
import com.theoyu.oursphere.kv.biz.service.NoteContentService;
import com.theoyu.oursphere.kv.dto.request.AddNoteContentReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NoteContentServiceImpl implements NoteContentService {
    @Resource
    private NoteContentRepository noteContentRepository;
    @Override
    public Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO) {
        // 笔记 ID
        Long noteId = addNoteContentReqDTO.getNoteId();
        // 笔记内容
        String content = addNoteContentReqDTO.getContent();

        // 构建数据库实体类
        NoteContentPO nodeContent = NoteContentPO.builder()
                .id(UUID.randomUUID())
                .content(content)
                .build();

        // 插入数据
        noteContentRepository.save(nodeContent);

        return Response.success();
    }
}
