package com.theoyu.oursphere.kv.biz.service.impl;

import com.theoyu.framework.common.exception.BusinessException;
import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.kv.biz.enums.ResponseCodeEnum;
import com.theoyu.oursphere.kv.biz.model.entity.NoteContentPO;
import com.theoyu.oursphere.kv.biz.model.repository.NoteContentRepository;
import com.theoyu.oursphere.kv.biz.service.NoteContentService;
import com.theoyu.oursphere.kv.dto.request.AddNoteContentReqDTO;
import com.theoyu.oursphere.kv.dto.request.DeleteNoteContentReqDTO;
import com.theoyu.oursphere.kv.dto.request.FindNoteContentReqDTO;
import com.theoyu.oursphere.kv.dto.response.FindNoteContentRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class NoteContentServiceImpl implements NoteContentService {
    @Resource
    private NoteContentRepository noteContentRepository;
    @Override
    public Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO) {
        // 笔记 ID
        String uuid = addNoteContentReqDTO.getUuid();
        // 笔记内容
        String content = addNoteContentReqDTO.getContent();

        // 构建数据库实体类
        NoteContentPO nodeContent = NoteContentPO.builder()
                .id(UUID.fromString(uuid))
                .content(content)
                .build();

        // 插入数据
        noteContentRepository.save(nodeContent);

        return Response.success();
    }

    @Override
    public Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO) {
        // 笔记 ID
        String uuid = findNoteContentReqDTO.getUuid();
        // 根据笔记 ID 查询笔记内容
        Optional<NoteContentPO> optional = noteContentRepository.findById(UUID.fromString(uuid));

        // 若笔记内容不存在
        if (optional.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.NOTE_CONTENT_NOT_FOUND);
        }

        NoteContentPO noteContentPO = optional.get();
        // 构建返参 DTO
        FindNoteContentRspDTO findNoteContentRspDTO = FindNoteContentRspDTO.builder()
                .uuid(noteContentPO.getId())
                .content(noteContentPO.getContent())
                .build();

        return Response.success(findNoteContentRspDTO);
    }

    @Override
    public Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        // 笔记 ID
        String uuid = deleteNoteContentReqDTO.getUuid();
        // 删除笔记内容
        noteContentRepository.deleteById(UUID.fromString(uuid));

        return Response.success();
    }
}
