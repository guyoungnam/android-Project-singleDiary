package org.techtown.diary;

public interface OnTabItemSelectedListener {

    public void onTableSelected(int position);
    public void showFragment2(Note item);

    //이 메서드가 호출되면 하단 탭의 setSelected 매서드를 이용해 다른 탭 버튼이 선택됨

}
