package org.shelltest.service.services;

import org.shelltest.service.entity.Property;
import org.shelltest.service.entity.PropertyExample;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.PropertyMapper;
import org.shelltest.service.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class PropertyService {

    @Autowired
    PropertyMapper propertyMapper;

    /**
     * 使用获取符合条件的第一条记录.
     * */
    public Property getPropertyByKeys (String type, String key) {
        PropertyExample serverExample = new PropertyExample();
        serverExample.createCriteria().andTypeEqualTo(type).andKeyEqualTo(key);
        List<Property> list = propertyMapper.selectByExample(serverExample);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }
    public List<Property> getPropertyListByKeys (String type, String key) {
        PropertyExample serverExample = new PropertyExample();
        serverExample.createCriteria().andTypeEqualTo(type).andKeyEqualTo(key);
        List<Property> list = propertyMapper.selectByExample(serverExample);
        return (list==null||list.size()==0)?null:list;
    }

    /**
     * 获取目标值的集合.
     * @param type 目标记录的type
     * @param key 目标记录的key
     * @return 返回指定type和key的多条记录
     * */
    public List<String> getValueListByKeys (String type, String key) {
        PropertyExample serverExample = new PropertyExample();
        serverExample.createCriteria().andTypeEqualTo(type).andKeyEqualTo(key).andValIsNotNull();
        List<String> list = propertyMapper.selectValueByExample(serverExample);
        return (list==null||list.size()==0)?null:list;
    }

    /**
     * 从服务器的一套配置中根据字段获取自己想要的配置.
     * @param list 服务器配置
     * @param type 需要的字段，如user/pass/*path
     * */
    public String getValueByType(List<Property> list, String type) throws MyException {
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

    public List<String> getServerListBySeqList (String key, List<Integer> seqList) {
        PropertyExample serverExample = new PropertyExample();
        serverExample.createCriteria().andTypeEqualTo(Constant.PropertyType.IP).andKeyEqualTo(key)
                .andSeqIn(seqList);
        List<Property> properties = propertyMapper.selectByExample(serverExample);
        if (properties == null)
            return null;
        List<String> list = new LinkedList<>();
        for (int i = 0; i < properties.size(); i++) {
            list.add(properties.get(i).getVal());
        }
        return list.isEmpty()?null:list;
    }

    public List<String> getAppPrefixList() {
        return joinWordList(getValueListByKeys(Constant.PropertyType.JAR_RENAME, Constant.PropertyKey.JAR_PREFIX));
    }
    public List<String> getAppSuffixList() {
        return joinWordList(getValueListByKeys(Constant.PropertyType.JAR_RENAME, Constant.PropertyKey.JAR_SUFFIX));
    }
    private List<String> joinWordList(List<String> list) {
        if (list == null)
            return null;
        List<String> retList = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            String[] wordList = list.get(i).split(" ");
            for (int j = 0; j < wordList.length; j++) {
                retList.add(wordList[j]);
            }
        }
        return retList;
    }
}
