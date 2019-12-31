package org.shelltest.service.controller;

import org.shelltest.service.dto.ServerDTO;
import org.shelltest.service.entity.History;
import org.shelltest.service.entity.Property;
import org.shelltest.service.entity.Repo;
import org.shelltest.service.entity.User;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.mapper.UserMapper;
import org.shelltest.service.services.PropertyService;
import org.shelltest.service.services.RepoService;
import org.shelltest.service.utils.Constant;
import org.shelltest.service.utils.ResponseBuilder;
import org.shelltest.service.utils.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RequestMapping("/system")
@RestController
public class SystemController {
    @Autowired
    HistoryMapper historyMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    PropertyService propertyService;
    @Autowired
    RepoService repoService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/read-message")
    public ResponseEntity readDeployMessage(Integer messageId) {
        logger.info("/system/read-message");
        History history = new History();
        history.setIsRead("1");
        history.setMessageId(messageId);
        historyMapper.updateByPrimaryKeySelective(history);
        return new ResponseBuilder().getResponseEntity();
    }

    @GetMapping("/getUsers")
    public ResponseEntity getAllUsers() {
        logger.info("/system/getUsers");
        List<User> users = userMapper.getAllUsers();
        return new ResponseBuilder().setData(users).getResponseEntity();
    }

    @GetMapping("/getServers")
    public ResponseEntity getAllServers() throws MyException {
        logger.info("/system/getServers");
        List<ServerDTO> serverDTOList = new LinkedList<>();
        List<Property> properties = propertyService.getPropertyListByType(Constant.PropertyType.IP);
        if (properties != null) {
            for (int i = 0; i < properties.size(); i++) {
                List<Property> serverInfo = propertyService.getServerInfo(properties.get(i).getVal());
                ServerDTO serverDTO = new ServerDTO(properties.get(i).getVal(),properties.get(i).getKey());
                serverDTO.fillData(propertyService, serverInfo);
                serverDTOList.add(serverDTO);
            }
        }
        return new ResponseBuilder().setData(serverDTOList).getResponseEntity();
    }

    @GetMapping("/getRepos")
    public ResponseEntity getAllRepos() {
        logger.info("/system/getRepos");
        List<Repo> repos = repoService.getRepositoryByType(null);
        return new ResponseBuilder().setData(repos).getResponseEntity();
    }
}
