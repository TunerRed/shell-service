package org.shelltest.service.services;

import org.shelltest.service.entity.History;
import org.shelltest.service.mapper.HistoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    HistoryMapper historyMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());

}
