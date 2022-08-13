package com.july.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.july.reggie.commom.R;
import com.july.reggie.entity.Employee;
import com.july.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

/**
 * @author july
 */

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param session
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpSession session, @RequestBody Employee employee) {

        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        if (emp == null) {
            return R.error("登陆失败！用户名或者密码错误！！");
        }

        if (!emp.getPassword().equals(password)) {
            return R.error("登陆失败！用户名或者密码错误！！");
        }

        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        session.setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 退出登录
     *
     * @param session
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpSession session) {
        session.removeAttribute("employee");
        return R.success("退出成功！");
    }

    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpSession session, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<Employee>> page(int page, int pageSize, String name) {
        Page<Employee> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 更新员工信息
     *
     * @param session
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpSession session, @RequestBody Employee employee) {
        Long empId = (Long) session.getAttribute("employee");
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 获取员工信息
     *
     * @param id 员工ID
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable String id) {
        Employee employee = employeeService.getById(id);

        if (employee != null) {
            return R.success(employee);
        }

        return R.error("没有找到用户信息");
    }
}