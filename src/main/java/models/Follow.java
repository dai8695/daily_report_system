package models;


import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import constants.JpaConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

        /**
         * フォローデータのDTOモデル
         *
         */
        @Table(name = JpaConst.TABLE_FOL)
        @NamedQueries({
            @NamedQuery(
                    name = JpaConst.Q_FOL_COUNT_ALL_ENP,
                    query = JpaConst.Q_FOL_COUNT_ALL_ENP_DEF),

        })


        @Getter //全てのクラスフィールドについてgetterを自動生成する(Lombok)
        @Setter //全てのクラスフィールドについてsetterを自動生成する(Lombok)
        @NoArgsConstructor //引数なしコンストラクタを自動生成する(Lombok)
        @AllArgsConstructor //全てのクラスフィールドを引数にもつ引数ありコンストラクタを自動生成する(Lombok)
        @Entity
        public class Follow {
            /**
             * id
             */
            @Id
            @Column(name = JpaConst.FOL_COL_ID)
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Integer id;

            /**
             * フォローした従業員の社員番号
             */

            @ManyToOne
            @JoinColumn(name = JpaConst.FOL_COL_FOL_EMP, nullable = false)
            private  Employee employeefollow;


            /**
             * フォローされている従業員の社員番号
             */
            @ManyToOne
            @JoinColumn(name = JpaConst.FOL_COL_FOLLOWER_EMP, nullable = false)
            private  Employee employeefollower;

            /**
             * 登録日時
             */
            @Column(name = JpaConst.LIK_COL_CREATED_AT, nullable = false)
            private LocalDateTime createdAt;

            /**
             * 更新日時
             */
            @Column(name = JpaConst.LIK_COL_UPDATED_AT, nullable = false)
            private LocalDateTime updatedAt;


        }



