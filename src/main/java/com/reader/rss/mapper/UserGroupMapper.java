package com.reader.rss.mapper;

import com.reader.rss.pojo.UserGroup;

public interface UserGroupMapper {
    int deleteByPrimaryKey(Integer groupId);

    int insert(UserGroup record);

    int insertSelective(UserGroup record);

    UserGroup selectByPrimaryKey(Integer groupId);

    int updateByPrimaryKeySelective(UserGroup record);

    int updateByPrimaryKey(UserGroup record);
}