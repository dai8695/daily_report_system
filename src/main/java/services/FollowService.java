package services;

import java.time.LocalDateTime;
import java.util.List;

import actions.views.EmployeeConverter;
import actions.views.EmployeeView;
import actions.views.FollowConverter;
import actions.views.FollowView;
import actions.views.ReportConverter;
import actions.views.ReportView;
import constants.JpaConst;
import models.Report;

public class FollowService extends ServiceBase{


    public void create(FollowView fv) {
        LocalDateTime ldt = LocalDateTime.now();
        fv.setCreatedAt(ldt);
        fv.setUpdatedAt(ldt);
        createInternal(fv);
    }
    /**
     * フォローデータを1件登録する
     * @param fv フォローデータ
     */
    private void createInternal(FollowView fv) {

        em.getTransaction().begin();
        em.persist(FollowConverter.toModel(fv));
        em.getTransaction().commit();

     }
    /**
     * ログイン中の従業員が指定した日報の作成者をフォローした件数を取得し、返却する
     * @param employee フォローした従業員
     * @param employee1 フォローされた従業員
     * @return 日報データの件数
     */
    public long countAllMine(EmployeeView employee ,EmployeeView employee1) {

        long count = (long) em.createNamedQuery(JpaConst. Q_FOL_COUNT_ALL_ENP, Long.class)
                .setParameter(JpaConst.JPQL_PARM_EMPLOYEE_FOL, EmployeeConverter.toModel(employee))
                .setParameter(JpaConst.JPQL_PARM_EMPLOYEE_FOLER, EmployeeConverter.toModel(employee1))
                .getSingleResult();

        return count;
    }

    /**
     * 指定した従業員がフォローした従業員の日報を取得し、返却する
     * @param employee
     * @return 日報データの件数
     */

    public  List<ReportView> getFollowPerPage(EmployeeView employee,int page) {

        List<Report> reports = em.createNamedQuery(JpaConst.Q_FOL_GET_REP,Report.class)
                .setParameter(JpaConst.JPQL_PARM_EMPLOYEE_FOL, EmployeeConverter.toModel(employee))
                .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
                .setMaxResults(JpaConst.ROW_PER_PAGE)
                .getResultList();
        return ReportConverter.toViewList(reports);
    }
    /**
     * 指定した従業員がフォローした従業員の日報の件数を取得し、返却する
     * @param employee
     * @return 日報データの件数
     */
        public long countFollow(EmployeeView employee) {

            long count = (long) em.createNamedQuery(JpaConst.Q_FOL_REP_COUNT, Long.class)
                    .setParameter(JpaConst.JPQL_PARM_EMPLOYEE_FOL, EmployeeConverter.toModel(employee))
                    .getSingleResult();

            return count;
        }
}
