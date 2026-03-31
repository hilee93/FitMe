package com.ootd.fitme.domain.comment.service;

import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CommentQueryServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private FeedFixtureBuilder feedFixtureBuilder;

    @Autowired
    private EntityManager em;




}