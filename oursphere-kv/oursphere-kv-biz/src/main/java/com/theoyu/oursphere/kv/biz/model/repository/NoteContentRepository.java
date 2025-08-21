package com.theoyu.oursphere.kv.biz.model.repository;

import com.theoyu.oursphere.kv.biz.model.entity.NoteContentPO;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;


public interface  NoteContentRepository extends CassandraRepository<NoteContentPO, UUID>{


}
