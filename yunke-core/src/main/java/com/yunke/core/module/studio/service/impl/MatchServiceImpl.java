package com.yunke.core.module.studio.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunke.common.core.constant.SystemConstant;
import com.yunke.common.core.entity.QueryParam;
import com.yunke.common.core.entity.studio.Match;
import com.yunke.common.core.entity.studio.Members;
import com.yunke.common.core.util.SortUtil;
import com.yunke.core.module.studio.generator.IdGenerateUtil;
import com.yunke.core.module.studio.generator.TaskTypeConstant;
import com.yunke.core.module.studio.mapper.MatchMapper;
import com.yunke.core.module.studio.service.IMatchService;
import com.yunke.core.module.studio.service.IMembersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 比赛_任务表(Match)表服务实现类
 *
 * @author chachae
 * @since 2020-06-14 14:04:54
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class MatchServiceImpl extends ServiceImpl<MatchMapper, Match> implements IMatchService {

    private final IMembersService membersService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTask(Match match, String[] userId, String[] state) {
        //生成带任务类型的id
        match.setMatchId(IdGenerateUtil.nextId(TaskTypeConstant.MATCH));
        //保存任务
        this.save(match);

        //添加成员
        //比赛类型：个人，只有一个成员且为负责人
        if (match.getType() == 0) {
            this.membersService.save(new Members(Integer.parseInt(userId[0]), Integer.parseInt(state[0]), match.getMatchId()));
        } else {
         //比赛类型：团队
            ArrayList<Members> members = new ArrayList<>(userId.length);
            IntStream.range(0, userId.length).forEach(index -> {
                members.add(new Members(Integer.parseInt(userId[index]), Integer.parseInt(state[index]), match.getMatchId()));
            });
            this.membersService.saveBatch(members);
        }


    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(String[] ids) {
        //删除任务
        this.removeByIds(Arrays.asList(ids));
        //删除成员
        Stream.of(ids).forEach(id -> this.membersService.remove(new LambdaQueryWrapper<Members>().eq(Members::getTaskId, id)));


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(Match match) {
        //进行中的任务 or报销成功后的报销字段修改
        if (match.getState() == 1||(match.getReimbursement()!=null&&match.getReimbursement()==1)) {
            this.updateById(match); //修改论文任务
        }

    }

    @Override
    public IPage<Match> pageTaskList(QueryParam param, Match match) {
        Page<Match> page = new Page<>(param.getPageNum(), param.getPageSize());
        SortUtil.handlePageSort(param, page, "title", SystemConstant.ORDER_ASC, true);
        return baseMapper.pageTask(page, match);
    }

    @Override
    public Map<String, Object> getTask(String matchId) {
        return baseMapper.getTask(matchId);
    }

    @Override
    public Integer getAllTaskCount() {
        return this.count();
    }
}