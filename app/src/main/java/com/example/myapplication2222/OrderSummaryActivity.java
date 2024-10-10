package com.example.myapplication2222;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class OrderSummaryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private KartriderAdapter productAdapter;
    private TextView totalQuantityTextView, totalPriceTextView;
    private FirebaseFirestore firestore;
    private CollectionReference cartCollectionRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recycler_view_order_summary);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Firebase Firestore 초기화
        firestore = FirebaseFirestore.getInstance();
        cartCollectionRef = firestore.collection("kartrider");

        // 총 수량 및 총 금액 TextView 초기화
        totalQuantityTextView = findViewById(R.id.total_quantity);
        totalPriceTextView = findViewById(R.id.total_amount_summary);

        // 장바구니 데이터를 로드하고 UI를 업데이트합니다.
        loadCartData();

        // 결제하기 버튼 설정
        Button payButton = findViewById(R.id.pay_button_summary);
        payButton.setOnClickListener(v -> navigateToPayment());
    }

    private void loadCartData() {
        cartCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    ArrayList<Kartrider> cartProducts = new ArrayList<>();
                    int totalPrice = 0;
                    int totalQuantity = 0;

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Kartrider cartProduct = document.toObject(Kartrider.class);
                        if (cartProduct != null) {
                            cartProduct.setId(document.getId()); // ensure the ID is set
                            cartProducts.add(cartProduct);

                            // 가격 및 수량 집계
                            totalPrice += cartProduct.getPrice() * cartProduct.getQuantity();
                            totalQuantity += cartProduct.getQuantity();
                        }
                    }

                    // ProductAdapter 초기화
                    productAdapter = new KartriderAdapter(cartProducts, null, this, true); // true 플래그 추가
                    recyclerView.setAdapter(productAdapter);

                    // 총 수량 및 총 금액 업데이트
                    updateSummary(totalPrice, totalQuantity);
                }
            } else {
                Toast.makeText(OrderSummaryActivity.this, "장바구니 데이터 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary(int totalPrice, int totalQuantity) {
        totalQuantityTextView.setText(getColoredText("총 수량: ", totalQuantity + "개"));
        totalPriceTextView.setText(getColoredText("총 결제금액: ", totalPrice + "원"));
    }

    private Spannable getColoredText(String prefix, String value) {
        Spannable spannable = new SpannableString(prefix + value);

        // prefix 부분을 검정색으로 설정
        int prefixEnd = prefix.length();
        spannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, prefixEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 숫자 부분의 색상을 빨간색으로 설정
        int numberStart = prefixEnd;
        int numberEnd = numberStart;
        int unitStart = prefixEnd;
        int unitEnd = spannable.length();

        // 숫자와 단위 구분
        String[] parts = value.split("(?<=\\d)(?=\\D)");
        if (parts.length == 2) {
            numberEnd = numberStart + parts[0].length();
            unitStart = numberEnd;
            unitEnd = unitStart + parts[1].length();
        }

        // 숫자 부분의 색상 변경
        if (numberEnd > numberStart) {
            spannable.setSpan(new ForegroundColorSpan(Color.RED), numberStart, numberEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 단위 부분의 색상 변경
        if (unitEnd > unitStart) {
            spannable.setSpan(new ForegroundColorSpan(Color.BLACK), unitStart, unitEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }

    private void navigateToPayment() {
        // PaymentActivity로 이동
        Intent intent = new Intent(OrderSummaryActivity.this, PaymentActivity.class);
        startActivity(intent);
        finish(); // 현재 Activity 종료
    }
}
