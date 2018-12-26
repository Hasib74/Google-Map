package com.example.dcl.androiduberapplication.Service;

import com.example.dcl.androiduberapplication.Common.Common;
import com.example.dcl.androiduberapplication.Model.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        updateTokenServer(refreshToken);
    }

    private void updateTokenServer(String refreshToken) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference(Common.token_tb1);
        Token token=new Token(refreshToken);

        if (FirebaseAuth.getInstance().getCurrentUser().getUid()!=null){
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
        }

    }
}
