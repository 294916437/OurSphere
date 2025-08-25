package com.theoyu.oursphere.note.biz.model.mapper;

import com.theoyu.oursphere.note.biz.model.entity.TopicPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TopicPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TopicPO record);

    int insertSelective(TopicPO record);

    TopicPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TopicPO record);

    int updateByPrimaryKey(TopicPO record);

    String selectNameByPrimaryKey(Long id);

    TopicPO selectByTopicName(String topicName);

    List<TopicPO> selectByTopicIdIn(List<Long> topicIds);

    int batchInsert(@Param("newTopics") List<TopicPO> newTopics);
}