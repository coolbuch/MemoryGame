package com.example.memorycanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

class Card {
    Paint p = new Paint();

    public Card(float x, float y, float width, float height, int color) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Card(Place p, int color) {
        this.color = color;
        this.x = p.x1;
        this.y = p.y1;
        this.width = p.w;
        this.height = p.h;
    }

    int color, backColor = Color.DKGRAY;
    boolean isOpen = false; // цвет карты
    float x, y, width, height;
    public void draw(Canvas c) {
        // нарисовать карту в виде цветного прямоугольника
        if (isOpen) {
            p.setColor(color);
        } else p.setColor(backColor);
        c.drawRect(x + 2,y + 2, x+width, y+height, p);
    }
    public boolean flip (float touch_x, float touch_y) {
        if (touch_x >= x && touch_x <= x + width && touch_y >= y && touch_y <= y + height) {
            isOpen = ! isOpen;
            return true;
        } else return false;
    }

    public boolean equals(Card c) {
        return c.color == this.color;
    }
}

class Place
{
    public int x1, y1, w, h;

    public Place(int x1, int y1, int w, int h)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.w = w;
        this.h = h;
    }
}

public class TilesView extends View {
    // пауза для запоминания карт
    final int PAUSE_LENGTH = 2; // в секундах
    boolean isOnPauseNow = false;

    // число открытых карт
    int openedCard = 0;

    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<Place> places = new ArrayList<>();
    int width, height; // ширина и высота канвы

    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        newGame();
        // 1) заполнить массив tiles случайными цветами
        // сгенерировать поле 2*n карт, при этом
        // должно быть ровно n пар карт разных цветов

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = canvas.getWidth();
        height = canvas.getHeight();
        // 2) отрисовка плиток
        // задать цвет можно, используя кисть
        Paint p = new Paint();
        for (Card c: cards) {
            c.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 3) получить координаты касания
        int x = (int) event.getX();
        int y = (int) event.getY();
        // 4) определить тип события

        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow)
        {
            // палец коснулся экрана
            for (Card c: cards) {

                if (openedCard == 0) {
                    if (c.flip(x, y)) {
                        Log.d("mytag", "card flipped: " + openedCard);
                        openedCard ++;
                        invalidate();
                        return true;
                    }
                }

                if (openedCard == 1) {


                    // перевернуть карту с задержкой
                    if (c.flip(x, y)) {

                        openedCard ++;

                        // 1) если открылис карты одинакового цвета, удалить их из списка
                        // например написать функцию, checkOpenCardsEqual

                        // 2) проверить, остались ли ещё карты
                        // иначе сообщить об окончании игры

                        // если карты открыты разного цвета - запустить задержку
                        checkOpenedCards(c);
                        if(cards.isEmpty())
                        {
                            Toast.makeText(getContext(), "Игра окончена", Toast.LENGTH_SHORT).show();
                        }
                        invalidate();

                        PauseTask task = new PauseTask();
                        task.execute(PAUSE_LENGTH);
                        isOnPauseNow = true;


                        return true;


                    }
                }

            }
        }


         // заставляет экран перерисоваться
        return true;
    }

    public boolean checkOpenedCards(Card openCard)
    {
        for (Card c : cards)
        {
            if (c.isOpen && openCard.color == c.color && c != openCard)
            {
                cards.remove(c);
                cards.remove(openCard);
                return true;
            }
        }
        return false;
    }

    public void newGame()
    {
        if (cards.isEmpty())
        {
            int col = Color.GREEN;
            for (int i = 0; i < 6; i++) {

                for (int j = 0; j < 2; j++) {
                    places.add(new Place(j * 200, i * 200, 200, 200));
                }
            }
            int i = 0;

            for (int j = 0; j < 12; j++) {
                switch (i) {
                    case 2:
                        col = Color.RED;
                        break;
                    case 4:
                        col = Color.YELLOW;
                        break;
                    case 6:
                        col = Color.MAGENTA;
                        break;
                    case 8:
                        col = Color.BLUE;
                        break;
                    case 10:
                        col = Color.CYAN;
                        break;
                }
                Random r = new Random();
                int rint = r.nextInt(places.size());
                cards.add(new Card(places.get(rint), col));
                places.remove(rint);
                i++;
            }
            invalidate();
        }
        else
        {
            Toast.makeText(getContext(), "Поле еще не пустое!", Toast.LENGTH_SHORT).show();
        }
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            Log.d("mytag", "Pause started");
            try {
                Thread.sleep(integers[0] * 1000); // передаём число секунд ожидания
            } catch (InterruptedException e) {}
            Log.d("mytag", "Pause finished");
            return null;
        }

        // после паузы, перевернуть все карты обратно


        @Override
        protected void onPostExecute(Void aVoid) {
            for (Card c: cards) {
                if (c.isOpen) {
                    c.isOpen = false;
                }
            }
            openedCard = 0;
            isOnPauseNow = false;
            invalidate();
        }
    }
}