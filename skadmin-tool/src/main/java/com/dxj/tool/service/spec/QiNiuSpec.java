package com.dxj.tool.service.spec;

import com.dxj.tool.domain.QiniuContent;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dxj
 * @date 2019-05-31
 */
public class QiNiuSpec {
    public static Specification<QiniuContent> getSpec(QiniuContent qiniuContent) {
        return (Specification<QiniuContent>) (root, query, cb) -> {
            List<Predicate> list = new ArrayList<>();

            if(!ObjectUtils.isEmpty(qiniuContent.getKey())){
                //模糊
                list.add(cb.like(root.get("key").as(String.class), "%" + qiniuContent.getKey() + "%"));
            }

            Predicate[] p = new Predicate[list.size()];
            return cb.and(list.toArray(p));
        };
    }
}