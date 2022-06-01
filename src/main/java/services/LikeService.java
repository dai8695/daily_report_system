package services;


import java.time.LocalDateTime;
import java.util.List;

import actions.views.EmployeeConverter;
import actions.views.EmployeeView;
import actions.views.LikeConverter;
import actions.views.LikeView;
import actions.views.ReportConverter;
import actions.views.ReportView;
import constants.JpaConst;
import models.Like;


public class LikeService extends ServiceBase {


        public void create(LikeView lv) {
            LocalDateTime ldt = LocalDateTime.now();
            lv.setCreatedAt(ldt);
            lv.setUpdatedAt(ldt);
            createInternal(lv);
        }
        /**
         * いいねデータを1件登録する
         * @param lv いいねデータ
         */
        private void createInternal(LikeView lv) {

            em.getTransaction().begin();
            em.persist(LikeConverter.toModel(lv));
            em.getTransaction().commit();

        }
        /**
         * 指定した日報の登録内容のいいねした人のデータを、ページ数の一覧画面に表示する分取得しLikeViewのリストで返却する
         * @param rv 日報の登録内容
         * @param page ページ数
         * @return 一覧画面に表示するデータのリスト
        */
        public List<LikeView> getMinePerPage(ReportView rv, int page) {

            List<Like> likes = em.createNamedQuery(JpaConst.Q_LIK_GET_ALL_MINE, Like.class)
                    .setParameter(JpaConst.JPQL_PARM_REPORT, ReportConverter.toModel(rv))
                    .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
                    .setMaxResults(JpaConst.ROW_PER_PAGE)
                    .getResultList();
            return LikeConverter.toViewList(likes);
        }

        /**
         * 指定した日報にいいねした人の件数を取得し、返却する
         * @param report 日報
         * @return 日報データの件数
         */
        public long countAllMine(ReportView report) {

            long count = (long) em.createNamedQuery(JpaConst.Q_LIK_COUNT_ALL_MINE, Long.class)
                    .setParameter(JpaConst.JPQL_PARM_REPORT, ReportConverter.toModel(report))
                    .getSingleResult();
            return count;
        }

        /**
         * ログインしている従業員が指定した日報にいいねした人の件数を取得し、返却する
         * @param report 日報
         * @return 日報データの件数
         */
        public long countAllMine(ReportView report,EmployeeView employee) {

            long count = (long) em.createNamedQuery(JpaConst. Q_LIK_COUNT_ALL_REP_ENP, Long.class)
                    .setParameter(JpaConst.JPQL_PARM_REPORT, ReportConverter.toModel(report))
                    .setParameter(JpaConst.JPQL_PARM_EMPLOYEE, EmployeeConverter.toModel(employee))
                    .getSingleResult();

            return count;
        }

}