package util;

public enum BroadcastEnum
{
    invite_user_to_room, quit_room , text, file_upload, file_remove;

    public static BroadcastEnum fromInteger (int x)
    {
        switch(x)
        {
            case 0:
                return invite_user_to_room;
            case 1:
                return quit_room;
            case 2:
                return text;
            case 3:
                return file_upload;
            case 4:
                return file_remove;
        }
        return null;
    }
}
