package test.model;

import lombok.Getter;

public class Board {

    @Getter
    private String name;

    public Board(String name) {
        this.name = name;
    }

    public ArticleManage articleManage(){
        return new ArticleManage();
    }
}
