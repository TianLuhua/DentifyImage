package com.boyue.booyuedentifyimage.bean;

import java.util.List;

/**
 * Created by Tianluhua on 2018\11\15 0015.
 */
public class ResultBean {

    /**
     * has_more : false
     * log_id : 2896827775265419631
     * result_num : 1
     * result : [{"score":0.54658565460129,"brief":"笔笔，这个是笔","cont_sign":"2657851221,2311767958"}]
     */

    private boolean has_more;
    private long log_id;
    private int result_num;
    private List<ResultResponseBean> result;

    public boolean isHas_more() {
        return has_more;
    }

    public void setHas_more(boolean has_more) {
        this.has_more = has_more;
    }

    public long getLog_id() {
        return log_id;
    }

    public void setLog_id(long log_id) {
        this.log_id = log_id;
    }

    public int getResult_num() {
        return result_num;
    }

    public void setResult_num(int result_num) {
        this.result_num = result_num;
    }

    public List<ResultResponseBean> getResult() {
        return result;
    }

    public void setResult(List<ResultResponseBean> result) {
        this.result = result;
    }

    public static class ResultResponseBean {
        /**
         * score : 0.54658565460129
         * brief : 笔笔，这个是笔
         * cont_sign : 2657851221,2311767958
         */

        private double score;
        private String brief;
        private String cont_sign;

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getBrief() {
            return brief;
        }

        public void setBrief(String brief) {
            this.brief = brief;
        }

        public String getCont_sign() {
            return cont_sign;
        }

        public void setCont_sign(String cont_sign) {
            this.cont_sign = cont_sign;
        }

        @Override
        public String toString() {
            return "ResultResponseBean{" +
                    "score=" + score +
                    ", brief='" + brief + '\'' +
                    ", cont_sign='" + cont_sign + '\'' +
                    '}';
        }
    }
}
