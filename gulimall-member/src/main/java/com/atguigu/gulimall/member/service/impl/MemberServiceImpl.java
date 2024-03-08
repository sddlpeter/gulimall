package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistsException;
import com.atguigu.gulimall.member.exception.UserNameExistsException;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = new MemberEntity();

        // 设置默认等级
        MemberLevelEntity levelEntity = memberDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        // 检查用户名和手机号是否唯一, 为了让controller感知，这里采用异常机制
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());

        // 密码要进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        // 保存
        memberDao.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistsException {
        MemberDao memberDao = this.baseMapper;
        Integer mobile = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExistsException();

        }
    }

    @Override
    public void checkUserNameUnique(String username) throws UserNameExistsException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UserNameExistsException();

        }

    }

}