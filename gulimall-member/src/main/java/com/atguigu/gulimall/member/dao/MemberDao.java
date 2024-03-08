package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author peter
 * @email sddlpeter@gmail.com
 * @date 2024-01-05 21:07:50
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    MemberLevelEntity getDefaultLevel();
}
