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

    /**
     * 使用联合主键获取指定记录.
     * */
    public Property getPropertyByKeys (String type, String key) {
        PropertyExample serverExample = new PropertyExample();
        serverExample.createCriteria().andTypeEqualTo(type).andKeyEqualTo(key);
        List<Property> list = propertyMapper.selectByExample(serverExample);
        return (list==null||list.size()==0)?null:list.get(0);
    }
    /**
     * 从服务器的一套配置中根据字段获取自己想要的配置.
     * @param list 服务器配置
     * @param type 需要的字段，如user/pass/*path
     * */
    public String getPropertyValueByType(List<Property> list, String type) throws MyException {
        if (list == null || type == null)
            throw new MyException(Constant.ResultCode.NOT_FOUND,"服务器找不到任何配置");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getType().equals(type))
                return list.get(i).getVal();
        }
        throw new MyException(Constant.ResultCode.NOT_FOUND,"服务器找不到对应配置："+type);
    }

    /**
     * 获取服务器信息.
     * 返回目标主机的一套配置，一个ip对应一套配置，若一个ip有多个用途/账户，它们对应同一套配置
     * @param serverIP 服务器地址
     * */
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
