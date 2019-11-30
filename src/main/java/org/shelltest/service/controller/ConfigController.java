package org.shelltest.service.controller;

import org.shelltest.service.entity.History;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.utils.ResponseBuilder;
import org.shelltest.service.utils.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/system")
@RestController
public class ConfigController {

    @Autowired
    HistoryMapper historyMapper;

    @GetMapping("/read-message")
    public ResponseEntity readDeployMessage(Integer messageId) {
        History history = new History();
        history.setIsRead("1");
        history.setMessageId(messageId);
        historyMapper.updateByPrimaryKeySelective(history);
        return new ResponseBuilder().getResponseEntity();
    }
}
