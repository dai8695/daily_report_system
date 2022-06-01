package actions.views;

import java.util.ArrayList;
import java.util.List;

import models.Like;

/**
 * いいねデータのDTOモデル⇔Viewモデルの変換を行うクラス
 *
 */
public class LikeConverter {
    /**
     * ViewモデルのインスタンスからDTOモデルのインスタンスを作成する
     * @param rv ReportViewのインスタンス
     * @return Reportのインスタンス
     */
    public static Like toModel(LikeView lv) {
        return new Like(
                lv.getId(),
                EmployeeConverter.toModel(lv.getEmployee()),
                ReportConverter.toModel(lv.getReport()),
                lv.getCreatedAt(),
                lv.getUpdatedAt());
    }

    /**
     * DTOモデルのインスタンスからViewモデルのインスタンスを作成する
     * @param r Reportのインスタンス
     * @return ReportViewのインスタンス
     */
    public static LikeView toView(Like l) {

        if (l == null) {
            return null;
        }

        return new LikeView(
                l.getId(),
                EmployeeConverter.toView(l.getEmployee()),
                ReportConverter.toView(l.getReport()),
                l.getCreatedAt(),
                l.getUpdatedAt());
    }

    /**
     * DTOモデルのリストからViewモデルのリストを作成する
     * @param list DTOモデルのリスト
     * @return Viewモデルのリスト
     */
    public static List<LikeView> toViewList(List<Like> list) {
        List<LikeView> evs = new ArrayList<>();

        for (Like l : list) {
            evs.add(toView(l));
        }

        return evs;
    }

    /**
     * Viewモデルの全フィールドの内容をDTOモデルのフィールドにコピーする
     * @param r DTOモデル(コピー先)
     * @param rv Viewモデル(コピー元)
     */
    public static void copyViewToModel(Like l, LikeView lv) {
        l.setId(lv.getId());
        l.setEmployee(EmployeeConverter.toModel(lv.getEmployee()));
        l.setReport(ReportConverter.toModel(lv.getReport()));
        l.setCreatedAt(lv.getCreatedAt());
        l.setUpdatedAt(lv.getUpdatedAt());

    }

}


