package com.jb.currencyexchange.dao;

import java.util.List;

public interface BaseDao<T> {
    T create(T entity);
    List<T> getAll();
    T update(T entity);
}
