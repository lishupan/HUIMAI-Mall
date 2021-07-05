package com.huimai.group;

import com.huimai.pojo.TbSpecification;
import com.huimai.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

/**
 * 规格和规格选项集合组合实体类
 */
public class Specification implements Serializable {
    //规格对象
    private TbSpecification tbSpecification;

    //规格选项集合
    private List<TbSpecificationOption> tbSpecificationOptionList;

    public TbSpecification getTbSpecification() {
        return tbSpecification;
    }

    public void setTbSpecification(TbSpecification tbSpecification) {
        this.tbSpecification = tbSpecification;
    }

    public List<TbSpecificationOption> getTbSpecificationOptionList() {
        return tbSpecificationOptionList;
    }

    public void setTbSpecificationOptionList(List<TbSpecificationOption> tbSpecificationOptionList) {
        this.tbSpecificationOptionList = tbSpecificationOptionList;
    }
}
