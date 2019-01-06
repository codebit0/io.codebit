package test.model;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import test.MybatisTransactionalProcessorTest;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArticleManage {

    @Transactional
    public List<Article> list() {
        List<Article> articles = new ArrayList<>();
        try(SqlSession session = MybatisTransactionalProcessorTest.sqlSessionFactory.openSession()) {
            System.out.println(session);
            List<Map<String,String>> list = session.selectList("test.model.selectArticles");
            articles = list.stream().map(m -> {
                return new Article(m.get("SUBJECT"), m.get("CONTENT"));
            }).collect(Collectors.toList());
        }
        return articles;
    }

    @Transactional
    public Article add(Article article) {
        try(SqlSession session = MybatisTransactionalProcessorTest.sqlSessionFactory.openSession()) {
            int i = session.insert("test.model.insertArticle", article);
            List<Map<String,String>> list = session.selectList("test.model.selectArticles");
            List<Article> articles = list.stream().map(m -> {
                return new Article(m.get("SUBJECT"), m.get("CONTENT"));
            }).collect(Collectors.toList());
            return articles.get(articles.size() -1);
        }
    }

    @Transactional
    public void addBoardAndArticle(Board board, Article article) {

    }
}
