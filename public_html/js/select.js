/*
 * При полной загрузке документа
 * мы начинаем определять события
 */
$(document).ready(function () {
    console.log("It is ready!!");
    $('#user_getAll').click(function () {
        console.log("Request: user_getAll!");
        var country_id = $(this).val();
        var url = '/db/api/user/getAll/';
        $.get(
            url,
            "",
            function (result) {
                console.log("Response on user_getAll!");
                if (result.type == 'error') {
                    alert('error');
                    return(false);
                }
                else {
                    console.log($(result.code));
                    $(result.response.userList).each(function() {
                        console.log((this));
                    });
                }
            },
            "json"
        );
    });
    $('#user_listFollowers').click(function () {
        console.log("Request: user_listFollowers!");
        $.ajax({
            url: "/db/api/user/listFollowers/?user=neikila2@gmail.com",
            type: "get",
            success: function(result){
                console.log("Response on user_listFollowers!");
                console.log($(result.code));
                console.log($(result));
                $(result.response.userList).each(function() {
                    console.log((this));
                });
            }
        });
    });

    $('#user_create').click(function () {
        console.log("Request: user_create!");
        var data = {
                "about": "hello im user1",
                "email": "example@mail.ru",
                "isAnonymous": true,
                "name": "John",
                "username": "user1"
            };
        $.ajax({
            url: "/db/api/user/create/",
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function(result){
                console.log("Response on user_create!");
                console.log($(result.code));
                console.log($(result));
            }
        });
    });

    $('#user_details').click(function () {
        console.log("Request: user_details!");
        var data = "user=example@mail.ru";
        $.ajax({
            url: "/db/api/user/details/?user=another95@mail.ru",
            type: "GET",
            contentType: "application/json",
            success: function(result){
                console.log("Response on user_details!");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });

    $('#user_follow').click(function () {
        console.log("Request: user_follow!");
        var data = {"followee": "example@mail.ru", "follower": "neikila@gmail.com"};
        $.ajax({
            url: "/db/api/user/follow/",
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function(result){
                console.log("Response on user_follow!");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });

    $('#user_unfollow').click(function () {
        console.log("Request: user_unfollow!");
        var data = {"followee": "example@mail.ru", "follower": "neikila@gmail.com"};
        $.ajax({
            url: "/db/api/user/unfollow/",
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function(result){
                console.log("Response on user_follow!");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });

    $('#user_unpdateProfile').click(function () {
        console.log("Request: user_unpdateProfile!");
        var data = {"about": "Wowowowow!!!", "user": "example@mail.ru", "name": "NewName2"};
        $.ajax({
            url: "/db/api/user/updateProfile/",
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function(result){
                console.log("Response on user_unpdateProfile!");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });

    $('#user_listPosts').click(function () {
        console.log("Request: user_listPosts!");
        $.ajax({
            url: "/db/api/user/listPosts/?since=2015-03-23+02%3A15%3A01&limit=2&user=neikila@gmail.com&order=asc",
            type: "GET",
            contentType: "application/json",
            success: function(result){
                console.log("Response on user_listPosts!");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });

    $('#forum_create').click(function () {
        console.log("Request: forum_create!");
        var data = {
            "name": "Forum ame",
            "short_name": "123asd",
            "user": "neikila@gmail.com"
        };
        $.ajax({
            url: "/db/api/forum/create/",
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function(result){
                console.log("Response on forum_create!");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });


    $('#forum_details').click(function () {
        console.log("Request: forum_details!");
        $.ajax({
            url: "/db/api/forum/details/?related=user&forum=test",
            type: "GET",
            contentType: "application/json",
            success: function(result){
                console.log("Response on forum_details!");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });


    $('#forum_listUsers').click(function () {
        console.log("Request: forum_listUsers!");
        $.ajax({
            url: "/db/api/forum/listUsers/?order=desc&forum=test",
            type: "GET",
            contentType: "application/json",
            success: function(result){
                console.log("Response on forum_listUsers!");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });


    $('#forum_listThreads').click(function () {
        console.log("Request: forum_listThreads!");
        $.ajax({
            url: "/db/api/forum/listThreads/?order=desc&forum=test&related=forum",
            type: "GET",
            contentType: "application/json",
            success: function(result){
                console.log("Response on forum_listThreads!");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });


    $('#thread_create').click(function () {
        console.log("Request: thread_create!");
        var data = {
            "forum": "test",
            "title": "qweqweqwe",
            "isClosed": true,
            "user": "neikila@gmail.com",
            "date": "2014-01-01 00:00:01",
            "message": "hey hey hey hey!",
            "slug": "trewqqwe",
            "isDeleted": true
        };
        $.ajax({
            url: "/db/api/thread/create/",
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function(result){
                console.log("Response on thread_create! ");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            },
            error: function(result){
                console.log("Response on thread_create! error");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });

    $('#thread_details').click(function () {
        console.log("Request: thread_details!");
        $.ajax({
            url: "/db/api/thread/details/?thread=1&related=forum",
            type: "GET",
            contentType: "application/json",
            success: function(result){
                console.log("Response on thread_details! ");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            },
            error: function(result){
                console.log("Response on thread_details! error");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });

    });


    $('#post_create').click(function () {
        console.log("Request: post_create!");
        var data = {
            "isApproved": true,
            "user": "example@mail.ru",
            "date": "2014-01-01 00:10:01",
            "message": "my message 1",
            "isSpam": false,
            "isHighlighted": true,
            "thread": 1,
"parent": 16,
            "forum": "test",
            "isDeleted": false,
            "isEdited": true
        };
        $.ajax({
            url: "/db/api/post/create/",
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function(result){
                console.log("Response on post_create! ");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            },
            error: function(result){
                console.log("Response on post_create! error");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });
    });

    $('#post_details').click(function () {
        console.log("Request: post_details!");
        $.ajax({
            url: "/db/api/post/details/?post=1&related=forum",
            type: "GET",
            contentType: "application/json",
            success: function(result){
                console.log("Response on post_details! ");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            },
            error: function(result){
                console.log("Response on post_details! error");
                console.log("Code: " + $(result).attr('code'));
                console.log($(result));
            }
        });

    });
});