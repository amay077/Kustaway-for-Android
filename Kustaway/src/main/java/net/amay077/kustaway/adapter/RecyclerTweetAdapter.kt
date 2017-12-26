package net.amay077.kustaway.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import de.greenrobot.event.EventBus
import net.amay077.kustaway.ProfileActivity
import net.amay077.kustaway.R
import net.amay077.kustaway.databinding.RowTweetBinding
import net.amay077.kustaway.event.AlertDialogEvent
import net.amay077.kustaway.model.AccessTokenManager
import net.amay077.kustaway.model.FavRetweetManager
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.UserIconManager
import net.amay077.kustaway.settings.BasicSettings
import net.amay077.kustaway.util.*
import twitter4j.DirectMessage
import twitter4j.Status
import twitter4j.User

class RecyclerTweetView constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr){

    private val binding: RowTweetBinding

    init {
        binding = RowTweetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun update(row: Row) {
        val fontSize = BasicSettings.getFontSize()
        if (fontSize != 12)
        {
            binding.status.setTag(fontSize)
            binding.status.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
            binding.displayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
            binding.screenName.setTextSize(TypedValue.COMPLEX_UNIT_SP, (fontSize - 2).toFloat())
            binding.datetimeRelative.setTextSize(TypedValue.COMPLEX_UNIT_SP, (fontSize - 2).toFloat())
        }

        if (row.isDirectMessage())
        {
            val message = row.message
            renderMessage(binding, message)
        }
        else
        {
            val status = row.status

            val retweet = status!!.getRetweetedStatus()
            if (row.isFavorite())
            {
                renderStatus(binding, status, null, row.getSource())
            }
            else if (retweet == null)
            {
                renderStatus(binding, status, null, null)
            }
            else
            {
                renderStatus(binding, retweet, status, null)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderMessage(binding:RowTweetBinding, message: DirectMessage) {

        val userId = AccessTokenManager.getUserId()

        binding.doRetweet.setVisibility(View.GONE)
        binding.doFav.setVisibility(View.GONE)
        binding.retweetCount.setVisibility(View.GONE)
        binding.favCount.setVisibility(View.GONE)
        binding.menuAndViaContainer.setVisibility(View.VISIBLE)

        if (message.sender.id == userId)
        {
            binding.doReply.setVisibility(View.GONE)
        }
        else
        {
            binding.doReply.setVisibility(View.VISIBLE)
            binding.doReply.setOnClickListener(OnClickListener { ActionUtil.doReplyDirectMessage(message, context) })
        }

        binding.displayName.setText(message.sender.name)
        binding.screenName.setText(("@" + message.sender.screenName))
        binding.status.setText(("D " + message.recipientScreenName
                + " " + message.text))
        binding.datetime
                .setText(TimeUtil.getAbsoluteTime(message.createdAt))
        binding.datetimeRelative.setText(TimeUtil.getRelativeTime(message.createdAt))
        binding.via.setVisibility(View.GONE)
        binding.quotedTweet.setVisibility(View.GONE)
        binding.retweetContainer.setVisibility(View.GONE)
        binding.imagesContainer.setVisibility(View.GONE)
        binding.imagesContainerWrapper.setVisibility(View.GONE)
        UserIconManager.displayUserIcon(message.sender, binding.icon)
        binding.icon.setOnClickListener(OnClickListener { v ->
            val intent = Intent(v.context, ProfileActivity::class.java)
            intent.putExtra("screenName", message.sender.screenName)
            context.startActivity(intent)
        })
        binding.actionContainer.setVisibility(View.GONE)
        binding.lock.setVisibility(View.INVISIBLE)
    }

    @SuppressLint("SetTextI18n")
    private fun renderStatus(binding:RowTweetBinding, status: Status, retweet: Status?,
                             favorite:User?) {

        val userId = AccessTokenManager.getUserId()

        if (status.favoriteCount > 0)
        {
            binding.favCount.setText((status.favoriteCount).toString())
            binding.favCount.setVisibility(View.VISIBLE)
        }
        else
        {
            binding.favCount.setText("0")
            binding.favCount.setVisibility(View.INVISIBLE)
        }

        if (status.retweetCount > 0)
        {
            binding.retweetCount.setText((status.retweetCount).toString())
            binding.retweetCount.setVisibility(View.VISIBLE)
        }
        else
        {
            binding.retweetCount.setText("0")
            binding.retweetCount.setVisibility(View.INVISIBLE)
        }

        binding.doReply.setOnClickListener(OnClickListener { ActionUtil.doReplyAll(status, context) })

        binding.doRetweet.setOnClickListener(OnClickListener {
            if (status.user.isProtected) {
                MessageUtil.showToast(R.string.toast_protected_tweet_can_not_share)
                return@OnClickListener
            }
            val id = FavRetweetManager.getRtId(status)
            if (id != null) {
                if (id == 0L) {
                    MessageUtil.showToast(R.string.toast_destroy_retweet_progress)
                } else {
                    val dialog = DestroyRetweetDialogFragment()
                    val args = Bundle(1)
                    args.putSerializable("status", status)
                    dialog.arguments = args
                    EventBus.getDefault().post(AlertDialogEvent(dialog))
                }
            } else {
                val dialog = RetweetDialogFragment()
                val args = Bundle(1)
                args.putSerializable("status", status)
                dialog.arguments = args
                EventBus.getDefault().post(AlertDialogEvent(dialog))
            }
        })

        binding.doFav.setOnClickListener(OnClickListener {
            if (binding.doFav.getTag() == "is_fav") {
                binding.doFav.setTag("no_fav")
                binding.doFav.setTextColor(Color.parseColor("#666666"))
                ActionUtil.doDestroyFavorite(status.id)
            } else {
                binding.doFav.setTag("is_fav")
                binding.doFav.setTextColor(ContextCompat.getColor(context, R.color.holo_orange_light))
                ActionUtil.doFavorite(status.id)
            }
        })

        if (FavRetweetManager.getRtId(status) != null)
        {
            binding.doRetweet.setTextColor(ContextCompat.getColor(context, R.color.holo_green_light))
        }
        else
        {
            binding.doRetweet.setTextColor(Color.parseColor("#666666"))
        }

        if (FavRetweetManager.isFav(status))
        {
            binding.doFav.setTag("is_fav")
            binding.doFav.setTextColor(ContextCompat.getColor(context, R.color.holo_orange_light))
        }
        else
        {
            binding.doFav.setTag("no_fav")
            binding.doFav.setTextColor(Color.parseColor("#666666"))
        }

        binding.displayName.setText(status.user.name)
        binding.screenName.setText("@" + status.user.screenName)
        binding.datetimeRelative.setText(TimeUtil.getRelativeTime(status.createdAt))
        binding.datetime.setText(TimeUtil.getAbsoluteTime(status.createdAt))

        val via = StatusUtil.getClientName(status.source)
        binding.via.setText("via " + via)
        binding.via.setVisibility(View.VISIBLE)

//        /**
//         * デバッグモードの時だけ Kustaway for Android をハイライト
//         */
//        if (BuildConfig.DEBUG)
//        {
//            if (via == "Kustaway for Android")
//            {
//                if (mColorBlue == 0)
//                {
//                    mColorBlue = ThemeUtil.getThemeTextColor(R.attr.holo_blue)
//                }
//                binding.mVia.setTextColor(mColorBlue)
//            }
//            else
//            {
//                binding.mVia.setTextColor(Color.parseColor("#666666"))
//            }
//        }

        // favの場合
        if (favorite != null)
        {
            binding.actionIcon.setText(R.string.fontello_star)
            binding.actionIcon.setTextColor(ContextCompat.getColor(context, R.color.holo_orange_light))
            binding.actionByDisplayName.setText(favorite!!.name)
            binding.actionByScreenName.setText("@" + favorite!!.screenName)
            binding.retweetContainer.setVisibility(View.GONE)
            binding.menuAndViaContainer.setVisibility(View.VISIBLE)
            binding.actionContainer.setVisibility(View.VISIBLE)
        }
        else if (retweet != null)
        {

            // 自分のツイート
            if (userId == status.user.id)
            {
                binding.actionIcon.setText(R.string.fontello_retweet)
                binding.actionIcon.setTextColor(ContextCompat.getColor(context, R.color.holo_green_light))
                binding.actionByDisplayName.setText(retweet!!.user.name)
                binding.actionByScreenName.setText("@" + retweet!!.user.screenName)
                binding.retweetContainer.setVisibility(View.GONE)
                binding.menuAndViaContainer.setVisibility(View.VISIBLE)
                binding.actionContainer.setVisibility(View.VISIBLE)
            }
            else
            {
                if (BasicSettings.getUserIconSize() == "none")
                {
                    binding.retweetIcon.setVisibility(View.GONE)
                }
                else
                {
                    binding.retweetIcon.setVisibility(View.VISIBLE)
                    ImageUtil.displayRoundedImage(retweet!!.user.profileImageURL, binding.retweetIcon)
                }
                binding.retweetBy.setText("RT by " + retweet!!.user.name + " @" + retweet!!.user.screenName)
                binding.actionContainer.setVisibility(View.GONE)
                binding.menuAndViaContainer.setVisibility(View.VISIBLE)
                binding.retweetContainer.setVisibility(View.VISIBLE)
            }
        }
        else
        {

            // 自分へのリプ
            if (StatusUtil.isMentionForMe(status))
            {
                binding.actionIcon.setText(R.string.fontello_at)
                binding.actionIcon.setTextColor(ContextCompat.getColor(context, R.color.holo_red_light))
                binding.actionByDisplayName.setText(status.user.name)
                binding.actionByScreenName.setText("@" + status.user.screenName)
                binding.actionContainer.setVisibility(View.VISIBLE)
                binding.retweetContainer.setVisibility(View.GONE)
            }
            else
            {
                binding.actionContainer.setVisibility(View.GONE)
                binding.retweetContainer.setVisibility(View.GONE)
            }
            binding.menuAndViaContainer.setVisibility(View.VISIBLE)
        }// RTの場合

        if (status.user.isProtected)
        {
            binding.lock.setVisibility(View.VISIBLE)
        }
        else
        {
            binding.lock.setVisibility(View.INVISIBLE)
        }
        UserIconManager.displayUserIcon(status.user, binding.icon)
        binding.icon.setOnClickListener(OnClickListener { v ->
            val intent = Intent(v.context, ProfileActivity::class.java)
            intent.putExtra("screenName", status.user.screenName)
            context.startActivity(intent)
        })

        // RTの場合はRT元
        val statusString = StatusUtil.getExpandedText(status)
        binding.status.setText(StatusUtil.generateUnderline(statusString))

        // 引用ツイート
        val quotedStatus = status.quotedStatus
        if (quotedStatus != null)
        {
            binding.quotedDisplayName.setText(quotedStatus!!.user.name)
            binding.quotedScreenName.setText(quotedStatus!!.user.screenName)
            binding.quotedStatus.setText(quotedStatus!!.text)

            // プレビュー表示On
            if (BasicSettings.getDisplayThumbnailOn())
            {
                ImageUtil.displayThumbnailImages(context, binding.quotedImagesContainer, binding.imagesContainerWrapper, binding.quotedPlay, quotedStatus)
            }
            else
            {
                binding.quotedImagesContainer.setVisibility(View.GONE)
                binding.quotedImagesContainerWrapper.setVisibility(View.GONE)
            }
            binding.quotedTweet.setVisibility(View.VISIBLE)
        }
        else
        {
            binding.quotedTweet.setVisibility(View.GONE)
        }

        // プレビュー表示On
        if (BasicSettings.getDisplayThumbnailOn())
        {
            ImageUtil.displayThumbnailImages(context, binding.imagesContainer, binding.imagesContainerWrapper, binding.play, status)
        }
        else
        {
            binding.imagesContainer.setVisibility(View.GONE)
            binding.imagesContainerWrapper.setVisibility(View.GONE)
        }
    }

    class RetweetDialogFragment: DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val status = arguments.getSerializable("status") as Status? ?: return super.onCreateDialog(savedInstanceState)
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.confirm_retweet)
            builder.setMessage(status!!.text)
            builder.setNeutralButton(getString(R.string.button_quote)
            ) { dialog, which ->
                ActionUtil.doQuote(status, activity)
                dismiss()
            }
            builder.setPositiveButton(getString(R.string.button_retweet),
                    object: DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which:Int) {
                            ActionUtil.doRetweet(status!!.id)
                            dismiss()
                        }
                    }
            )
            builder.setNegativeButton(getString(R.string.button_cancel),
                    object: DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which:Int) {
                            dismiss()
                        }
                    }
            )
            return builder.create()
        }
    }

    class DestroyRetweetDialogFragment: DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val status = arguments.getSerializable("status") as Status? ?: return super.onCreateDialog(savedInstanceState)
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.confirm_destroy_retweet)
            builder.setMessage(status!!.text)
            builder.setPositiveButton(getString(R.string.button_destroy_retweet),
                    object: DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which:Int) {
                            ActionUtil.doDestroyRetweet(status)
                            dismiss()
                        }
                    }
            )
            builder.setNegativeButton(getString(R.string.button_cancel),
                    object: DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which:Int) {
                            dismiss()
                        }
                    }
            )
            return builder.create()
        }
    }


}

class RecyclerTweetViewHolder(private val view: RecyclerTweetView) : RecyclerView.ViewHolder(view) {

    var onItemClickListener: ((Int) -> Unit) = {}
    var onItemLongClickListener: ((Int) -> Boolean) = { false }

    init {
        view.setOnClickListener { _ ->
            onItemClickListener(adapterPosition)
        }

        view.setOnLongClickListener { _ ->
            onItemLongClickListener(adapterPosition)
        }
    }

    fun update(row : Row) {
        view.update(row)
    }
}

class RecyclerTweetAdapter(
        private val context: Context,
        private val rows: MutableList<Row>) :
        ProfileItemAdapter<Row>() {

    override var onItemClickListener: (Row) -> Unit = {}
    override var onItemLongClickListener: (Row) -> Boolean = { false }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = RecyclerTweetView(context)
        return RecyclerTweetViewHolder(view).also { holder ->
            holder.onItemClickListener = { pos -> onItemClickListener(rows[pos]) }
            holder.onItemLongClickListener = { pos -> onItemLongClickListener(rows[pos]) }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if(holder is RecyclerTweetViewHolder){
            holder.update(rows[position])
        }
    }

    override fun getItemCount(): Int {
        return rows.count()
    }

    override fun add(row: Row) {
        rows.add(row)
    }
}
