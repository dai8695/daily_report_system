package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import actions.views.FollowView;
import actions.views.LikeView;
import actions.views.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import services.FollowService;
import services.LikeService;
import services.ReportService;


/**
 * 日報に関する処理を行うActionクラス
 *
 */
public class ReportAction extends ActionBase {

    private ReportService service;
    private LikeService Likeservice;
    private FollowService Followservice;

    /**
     * メソッドを実行する
     */
    @Override
    public void process() throws ServletException, IOException {

        service = new ReportService();

        Likeservice = new LikeService();

        Followservice = new FollowService();

        //メソッドを実行
        invoke();
        service.close();
    }

    /**
     * 一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException, IOException {

        //指定されたページ数の一覧画面に表示する日報データを取得
        int page = getPage();
        List<ReportView> reports = service.getAllPerPage(page);

        //全日報データの件数を取得
        long reportsCount = service.countAll();

        putRequestScope(AttributeConst.REPORTS, reports); //取得した日報データ
        putRequestScope(AttributeConst.REP_COUNT, reportsCount); //全ての日報データの件数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //一覧画面を表示
        forward(ForwardConst.FW_REP_INDEX);
    }
    /**
     * 新規登録画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void entryNew() throws ServletException, IOException {

        putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン

        //日報情報の空インスタンスに、日報の日付＝今日の日付を設定する
        ReportView rv = new ReportView();
        rv.setReportDate(LocalDate.now());
        putRequestScope(AttributeConst.REPORT, rv); //日付のみ設定済みの日報インスタンス

        //新規登録画面を表示
        forward(ForwardConst.FW_REP_NEW);
   }
    /**
     * 新規登録を行う
     * @throws ServletException
     * @throws IOException
     */
    public void create() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken()) {

            //日報の日付が入力されていなければ、今日の日付を設定
            LocalDate day = null;
            if (getRequestParam(AttributeConst.REP_DATE) == null
                    || getRequestParam(AttributeConst.REP_DATE).equals("")) {
                day = LocalDate.now();
            } else {
                day = LocalDate.parse(getRequestParam(AttributeConst.REP_DATE));
            }

            //セッションからログイン中の従業員情報を取得
            EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

            //パラメータの値をもとに日報情報のインスタンスを作成する
            ReportView rv = new ReportView(
                    null,
                    ev, //ログインしている従業員を、日報作成者として登録する
                    day,
                    getRequestParam(AttributeConst.REP_TITLE),
                    getRequestParam(AttributeConst.REP_CONTENT),
                    null,
                    null,
                    0);

            //日報情報登録
            List<String> errors = service.create(rv);

            if (errors.size() > 0) {
                //登録中にエラーがあった場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.REPORT, rv);//入力された日報情報
                putRequestScope(AttributeConst.ERR, errors);//エラーのリスト

                //新規登録画面を再表示
                forward(ForwardConst.FW_REP_NEW);

            } else {
                //登録中にエラーがなかった場合

                //セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
            }
        }
    }
    /**
     * 詳細画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void show() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView loginEmployee = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //ログイン中の従業員が指定した日報にいいねした件数を取得
        long LikeCount = Likeservice.countAllMine(rv,loginEmployee);

        if (rv == null) {
            //該当の日報データが存在しない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

        } else {
            //ログイン中の従業員が指定した日報の作成者をフォローした件数を取得
            long FollowCount = Followservice.countAllMine(loginEmployee,rv.getEmployee());


            putRequestScope(AttributeConst.REPORT, rv); //取得した日報データ
            putRequestScope(AttributeConst.LIK_COUNT_REP_EMP, LikeCount); //いいねした件数
            putRequestScope(AttributeConst.FOLLOW_COUNT_EMP, FollowCount);//フォローした件数
            //詳細画面を表示
            forward(ForwardConst.FW_REP_SHOW);

        }
    }
    /**
     * 編集画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void edit() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        if (rv == null || ev.getId() != rv.getEmployee().getId()) {
            //該当の日報データが存在しない、または
            //ログインしている従業員が日報の作成者でない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

        } else {

            putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
            putRequestScope(AttributeConst.REPORT, rv); //取得した日報データ

            //編集画面を表示
            forward(ForwardConst.FW_REP_EDIT);
        }
    }
    /**
     * 更新を行う
     * @throws ServletException
     * @throws IOException
     */
    public void update() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken()) {

            //idを条件に日報データを取得する
            ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

            //入力された日報内容を設定する
            rv.setReportDate(toLocalDate(getRequestParam(AttributeConst.REP_DATE)));
            rv.setTitle(getRequestParam(AttributeConst.REP_TITLE));
            rv.setContent(getRequestParam(AttributeConst.REP_CONTENT));

            //日報データを更新する
            List<String> errors = service.update(rv);

            if (errors.size() > 0) {
                //更新中にエラーが発生した場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.REPORT, rv); //入力された日報情報
                putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                //編集画面を再表示
                forward(ForwardConst.FW_REP_EDIT);
            } else {
                //更新中にエラーがなかった場合

                //セッションに更新完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
            }
        }
      }
    /**
     * いいねデータを登録する
     * @throws ServletException
     * @throws IOException
     */
    public void likes () throws ServletException, IOException {


         //idを条件に日報データを取得する
         ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));
         //取得した日報データのいいね数に１を＋する
         int l = rv.getLikesCount();

         l++;

         rv.setLikesCount(l);
         //日報データを更新する
         service.update(rv);


         //セッションからログイン中の従業員情報を取得
         EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

         //パラメータの値を元にいいね情報のインスタンスを作成する
         LikeView lv = new LikeView(
                      null,
                      ev,
                      service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID))),
                      null,
                      null);

         //いいね情報登録
         Likeservice.create(lv);

         //セッションに更新完了のフラッシュメッセージを設定
         putSessionScope(AttributeConst.FLUSH, MessageConst.I_LIKES.getMessage());

         //一覧画面にリダイレクト
         redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
       }
    /**
     * いいねした人一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void likeindex() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //指定されたページ数の一覧画面に表示するいいねデータを取得
        int page = getPage();
        List<LikeView> likes = Likeservice.getMinePerPage(rv,page);

        //いいねデータの件数を取得
        long likeCount = Likeservice. countAllMine(rv);

        putRequestScope(AttributeConst.REPORT, rv);//取得した日報データ
        putRequestScope(AttributeConst.LIKES, likes); //取得したいいねデータ
        putRequestScope(AttributeConst.LIK_COUNT, likeCount); //いいねデータの件数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

           //一覧画面を表示
           forward(ForwardConst.FW_LIK_INDEX);
       }
    /**
     * フォローデータを登録する
     * @throws ServletException
     * @throws IOException
     */
    public void follows () throws ServletException, IOException {

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);


        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //パラメータの値を元にフォロー情報のインスタンスを作成する
        FollowView fv = new FollowView(
                     null,
                     ev,
                     rv.getEmployee(),
                     null,
                     null);

         //フォロー情報登録
         Followservice.create(fv);

         //セッションに更新完了のフラッシュメッセージを設定
         putSessionScope(AttributeConst.FLUSH, MessageConst.I_FOLLOW.getMessage());

         //タイムライン一覧画面にリダイレクト
         redirect(ForwardConst.ACT_REP, ForwardConst.CMD_TIMELINE);
       }
    /**
     * フォローした人の日報をタイムライン画面に表示する
     * @throws ServletException
     * @throws IOException
     */
    public void timeline () throws ServletException, IOException {
        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //指定されたページ数の一覧画面に表示するフォローされた人の日報データを取得
        int page = getPage();
        List<ReportView> reports = Followservice.getFollowPerPage(ev,page);

        //フォローされた人の日報データの件数を取得
        long FolloereportCount = Followservice.countFollow(ev);

        putRequestScope(AttributeConst.REPORTS, reports); //取得した日報データ
        putRequestScope(AttributeConst.REP_COUNT, FolloereportCount);//日報データの件数を取得
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //タイムライン画面を表示
          forward(ForwardConst.FW_FOL_INDEX);
     }

}






