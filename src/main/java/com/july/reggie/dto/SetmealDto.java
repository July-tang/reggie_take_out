package com.july.reggie.dto;


import com.july.reggie.entity.Setmeal;
import com.july.reggie.entity.SetmealDish;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;


}
