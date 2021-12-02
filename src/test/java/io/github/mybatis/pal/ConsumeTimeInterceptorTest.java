package io.github.mybatis.pal;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.List;

class ConsumeTimeInterceptorTest {

  private SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("io/github/mybatis/pal/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), "io/github/mybatis/pal/CreateDB.sql");
  }

  @Test
  void testUserList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Object> list = sqlSession.selectList("getUser");
      Assertions.assertEquals(1, list.size());
    }
  }

  @Test
  void testAddUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      User user = new User();
      user.setId(1);
      user.setName("User2");
      Assertions.assertEquals(1, sqlSession.insert("insertUser", user));
    }
  }

}
