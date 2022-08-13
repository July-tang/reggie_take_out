package com.july.reggie.dto;


import com.july.reggie.entity.OrderDetail;
import com.july.reggie.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class OrdersDto extends Orders {

    private int sumNum;

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
