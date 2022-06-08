package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {
    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();

        try {
            bizLogic(con, fromId, toId, money);
            con.commit();

        } catch (Exception e) {
            con.rollback();
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }
    }
    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        con.setAutoCommit(false);
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromMember.getMemberId(), fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toMember.getMemberId(), toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if ("ex".equals(toMember.getMemberId())) {
            throw new IllegalStateException("이체중 예외발생");
        }
    }
    private void release(Connection con) {
        try {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        } catch (Exception e) {
            log.info("error", e);
        }

    }

}
