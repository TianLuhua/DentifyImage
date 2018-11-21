package com.boyue.booyuedentifyimage.ui

import com.boyue.booyuedentifyimage.base.IBaseView
import com.boyue.booyuedentifyimage.base.IPresenter

/**
 * Created by Tianluhua on 2018\11\21 0021.
 */
class MainContract {
    interface View : IBaseView {

    }

    interface Presenter : IPresenter<View> {

    }

}