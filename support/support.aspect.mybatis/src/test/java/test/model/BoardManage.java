package test.model;

import org.apache.ibatis.session.SqlSession;
import test.MybatisTransactionalProcessorTest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoardManage {

    @Transactional
    public List<Board> list() {
        try(SqlSession session = MybatisTransactionalProcessorTest.sqlSessionFactory.openSession()) {
            List<Map<String,String>> list = session.selectList("test.model.selectBoards");
            List<Board> boards = list.stream().map(m -> {
                return new Board(m.get("NAME"));
            }).collect(Collectors.toList());
            return boards;
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {RuntimeException.class})
    public Board add(Board board) {
        try(SqlSession session = MybatisTransactionalProcessorTest.sqlSessionFactory.openSession(false)) {
            int i = session.insert("test.model.insertBoard", board);
            List<Map<String,String>> list = session.selectList("test.model.selectBoards");
            List<Board> boards = list.stream().map(m -> {
                return new Board(m.get("NAME"));
            }).collect(Collectors.toList());
            int size = boards.size();
            if(size > 2) {
                throw new RollbackException();
            }
            return boards.get(boards.size() -1);
        }
    }

    @Transactional
    public void adds(List<Board> boards) {
        for (Board board: boards) {
            try{
                add(board);
            }catch (Exception e) {

            }
        }
    }
}
