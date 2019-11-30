package org.shelltest.service.controller;

import org.shelltest.service.entity.History;
import org.shelltest.service.entity.HistoryExample;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.utils.ResponseBuilder;
import org.shelltest.service.utils.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RequestMapping("/common")
@RestController
public class CommonController {

    @Autowired
    HistoryMapper historyMapper;

    @GetMapping("/message-list")
    public ResponseEntity getDeployMessage() {
        int notReadCount = 0;
        List<History> totalList = historyMapper.selectNotRead(20);
        List<History> readList = null;
        if (totalList == null) {
            totalList = new LinkedList<>();
        }
        notReadCount = totalList.size();
        if (notReadCount < 10) {
            readList = historyMapper.selectAlreadyRead(10 - notReadCount);
            totalList.addAll(readList);
        }
        return new ResponseBuilder().setData(totalList).getResponseEntity();
    }
}
