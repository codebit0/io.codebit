package test;

import io.codebit.support.aspect.AdviceEvent;
import in.java.support.aspect.mybatis.transaction.MybatisTransactionalProcessor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import test.model.Article;
import test.model.ArticleManage;
import test.model.Board;
import io.codebit.support.aspect.transation.TransationalProcessor;
import io.codebit.support.bci.AspectClassFileTransformer;
import io.codebit.support.bci.LoadtimeInstrument;
import org.junit.Before;
import org.junit.Test;
import test.model.BoardManage;

import java.io.IOException;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MybatisTransactionalProcessorTest {

    public static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() throws Exception {
        AspectClassFileTransformer.WeavingContext context = new AspectClassFileTransformer.WeavingContext(AdviceEvent.class,
                TransationalProcessor.class, MybatisTransactionalProcessor.class);
        org.aspectj.bridge.CountingMessageHandler d;
        context.includes(Arrays.asList("test.model..*", "org.aspectj.*"));
//        context.options("-verbose -showWeaveInfo");
//        context.options("-verbose -showWeaveInfo -Xreweavable -XmessageHandlerClass:in.java.support.bci.WeaverMessagesHandle");
//        context.options("-verbose -XmessageHandlerClass:org.aspectj.weaver.loadtime.DefaultMessageHandler");
//        context.options("-verbose -showWeaveInfo -Xreweavable -XmessageHandlerClass:org.aspectj.weaver.loadtime.DefaultMessageHandler");
//        context.options("-verbose -showWeaveInfo -XmessageHandlerClass:in.java.support.bci.WeaverMessagesHandle");
//        context.options("-XmessageHandlerClass:org.aspectj.weaver.loadtime.DefaultMessageHandler");
//        context.options("-XmessageHandlerClass:org.aspectj.weaver.loadtime.DefaultMessageHandler");
        AspectClassFileTransformer transformer = new AspectClassFileTransformer(context);
        try {
            LoadtimeInstrument.transform(Arrays.asList(transformer));
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void list() throws IOException {
        System.out.println("ddd");
        org.apache.ibatis.session.SqlSession s;
        Board board = new Board("게시판");
        ArticleManage articleManage = board.articleManage();
        List<Article> list = articleManage.list();
        System.out.println(list);
//        articleManage.add();
    }

    @Test
    public void add() throws IOException {
        Board board = new Board("게시판");
        Article article = new Article("Article3", "Content3");
        ArticleManage articleManage = board.articleManage();
        Article add = articleManage.add(article);
        System.out.println(add);
//        articleManage.add();
    }

    @Test
    public void addBoards() throws IOException {
        BoardManage boardManage = new BoardManage();
        List<Board> boards = new ArrayList<>();
        for(int i=0; i < 5; i++) {
            Board board = new Board("게시판"+i);
            boards.add(board);
        }
        boardManage.adds(boards);
        List<Board> list = boardManage.list();
        System.out.println(list);
    }
}