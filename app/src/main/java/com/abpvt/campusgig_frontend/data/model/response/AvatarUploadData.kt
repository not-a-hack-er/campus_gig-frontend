package com.abpvt.campusgig_frontend.data.model.response

import com.abpvt.campusgig_frontend.data.model.User

data class AvatarUploadData(
    val avatarUrl: String = "",
    val user: User = User()
)
