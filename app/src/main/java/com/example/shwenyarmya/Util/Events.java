package com.example.shwenyarmya.Util;

public class Events {

    // Event used to send message from login notify.
    public static class Login {
        private String login;

        public Login(String login) {
            this.login = login;
        }

        public String getLogin() {
            return login;
        }
    }

    // Event used to send message from comment notify.
    public static class Comment {
        private String user_id, user_name, user_image, book_id, comment_text, comment_date;

        public Comment(String user_id, String user_name, String user_image, String book_id, String comment_text, String comment_date) {
            this.user_id = user_id;
            this.user_name = user_name;
            this.user_image = user_image;
            this.book_id = book_id;
            this.comment_text = comment_text;
            this.comment_date = comment_date;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getUser_name() {
            return user_name;
        }

        public String getUser_image() {
            return user_image;
        }

        public String getBook_id() {
            return book_id;
        }

        public String getComment_text() {
            return comment_text;
        }

        public String getComment_date() {
            return comment_date;
        }
    }

    // Event used to send message from favourite notify.
    public static class Favourite {

        private String string;

        public Favourite(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }

}
