package org.shelltest.service.services;

import org.shelltest.service.entity.Property;
import org.shelltest.service.entity.PropertyExample;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.PropertyMapper;
import org.shelltest.service.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    @Autowired
    PropertyMapper propertyMapper;

    public Property getPropertyByKeys (String type, String key) {
        PropertyExample serverExample = new PropertyExample();
        serverExample.createCriteria().andTypeEqualTo(type).andKeyEqualTo(key);
        List<Property> list = propertyMapper.selectByExample(serverExample);
        return (list==null||list.size()==0)?null:list.get(0);
    }
    public String getPropertyValueByType(List<Property> list, String type) throws MyException {
        if (list == null || type == null)
            throw new MyException(Constant.ResultCode.NOT_FOUND,"服务器找不到任何配置");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getType().equals(type))
                return list.get(i).getVal();
        }
        throw new MyException(Constant.ResultCode.NOT_FOUND,"服务器找不到对应配置："+type);
    }
    public List<Property> getServerInfo (String serverIP) throws MyException {
        PropertyExample serverExample = new PropertyExample();
        serverExample.createCriteria().andTypeEqualTo(Constant.PropertyType.IP).andValEqualTo(serverIP);
        List<Property> properties = propertyMapper.selectByExample(serverExample);
        if (properties == null || properties.size() != 1) {
            throw new MyException(Constant.ResultCode.NOT_FOUND,"找不到正确的服务器");
        }
        PropertyExample serverInfoExample = new PropertyExample();
        serverInfoExample.createCriteria().andKeyEqualTo(serverIP);
        List<Property> serverInfo = propertyMapper.selectByExample(serverInfoExample);
        return (serverInfo==null||serverInfo.isEmpty())?null:serverInfo;
    }
}
