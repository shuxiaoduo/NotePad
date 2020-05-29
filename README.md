# NotePad
This is an AndroidStudio rebuild of google SDK sample NotePad
## 一、添加时间戳
![image](https://github.com/shuxiaoduo/NotePad/blob/master/image/time.png)
<br>
### 主要代码
#### 1.在NoteList代码中修改projection添加创建时间显示时间戳
~~~
   private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_CREATE_DATE//添加时间戳
    };
~~~

#### 2.添加时间戳布局
在viewid中添加时间戳布局的id
~~~
  int[] viewIDs = { android.R.id.text1,R.id.text2 };//添加时间戳的布局位置

        // Creates the backing adapter for the ListView.
        SimpleCursorAdapter adapter
            = new SimpleCursorAdapter(
                      this,                             // The Context for the ListView
                      R.layout.noteslist_item,          // Points to the XML for a list item
                      cursor,                           // The cursor to get items from
                      new String[]{NotePad.Notes.COLUMN_NAME_TITLE,NotePad.Notes.COLUMN_NAME_CREATE_DATE},
                      viewIDs
              );
  ~~~
 #### 3.NoteList布局文件noteslist_item.xml
 ~~~
 <TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@android:id/text1"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="?android:attr/textAppearanceLarge"
    android:gravity="center_vertical"
    android:textSize="25dp"
    android:paddingLeft="5dip"
    android:singleLine="true"
/>
<!--新添加的textview显示时间戳-->
<TextView android:id="@+id/text2"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="?android:attr/textAppearanceLarge"
    android:gravity="center_vertical"
    android:textSize="16dp"
    android:textColor="@color/gray"
    android:paddingLeft="5dip"
    android:singleLine="true"
    />
</LinearLayout>
~~~
对于获取现在时间的处理，我把create_date改成了text类型
~~~
 @Override
       public void onCreate(SQLiteDatabase db) {
           db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                   + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                   + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " TEXT"
                   + ");");
       }
~~~
然后对时间进行处理添加到数据库中
~~~
  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss//获取当前时间
         Date date = new Date(System.currentTimeMillis());
         String now=simpleDateFormat.format(date);//存储字符串类型的时间
        // If the values map doesn't contain the creation date, sets the value to the current time.
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, now);
        }
 ~~~
## 二、添加笔记查询功能
#### 1.点击查询菜单出现搜索框
![image](https://github.com/shuxiaoduo/NotePad/blob/master/image/time.png)
![image](https://github.com/shuxiaoduo/NotePad/blob/master/image/search1.png)
#### 2.输入搜索内容实时搜索
![image](https://github.com/shuxiaoduo/NotePad/blob/master/image/search2.png)
![image](https://github.com/shuxiaoduo/NotePad/blob/master/image/search3.png)
#### 3.点击查询的结果可以进入查看对应的笔记内容
![image](https://github.com/shuxiaoduo/NotePad/blob/master/image/search4.png)
### 主要代码
#### 1.添加搜索菜单修改list_options_menu.xml
~~~
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <!--  This is our one standard application action (creating a new note). -->
    <item android:id="@+id/menu_add"
          android:icon="@drawable/ic_menu_compose"
          android:title="@string/menu_add"
          android:alphabeticShortcut='a'
          android:showAsAction="always" />
    <!--  If there is currently data in the clipboard, this adds a PASTE menu item to the menu
          so that the user can paste in the data.. -->
    <item android:id="@+id/menu_search"
        android:icon="@drawable/ic_search_black_24dp"
        android:title="搜索"
        android:showAsAction="always"
        />
    <item android:id="@+id/menu_paste"
          android:icon="@drawable/ic_menu_compose"
          android:title="@string/menu_paste"
          android:alphabeticShortcut='p' />
</menu>
~~~
#### 2.为搜索菜单选项添加事件修改NoteList.java
~~~
  case R.id.menu_search:
                Intent intent = new Intent(this,NoteSearch.class);
                startActivity(intent);
                return true;
~~~
#### 3.新建NoteSearch.class实现搜索功能
搜索功能支持实时搜索和模糊查询，并且可以在搜索结果当中点击进入对应的便签。<br>
使用searchView实现，为searchView添加监听事件。详细实现过程请看代码中注释。
~~~
package com.example.android.notepad;

/**
 * Created by Lenovo on 2020-05-11.
 */

public class NoteSearch extends Activity{
    private static final String TAG = "NoteSearch";
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_CREATE_DATE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏显示
        super.setContentView(R.layout.note_search);
        Intent intent = getIntent();

        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of notes.
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        final ListView listview= (ListView) findViewById(R.id.listview);//获取listview
        NotePadProvider.DatabaseHelper dh=new NotePadProvider.DatabaseHelper(this);
        final SQLiteDatabase db=dh.getReadableDatabase();//对数据库进行操作
        SearchView search= (SearchView) findViewById(R.id.search);//获取搜索视图
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {//添加监听事件
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {//实现模糊查询，通过标题或者内容进行查询
                Cursor cursor=db.query(//数据库查询
                        NotePad.Notes.TABLE_NAME,
                        PROJECTION,
                        NotePad.Notes.COLUMN_NAME_TITLE+" like ? or "+NotePad.Notes.COLUMN_NAME_NOTE+" like ?",//查询条件
                        new String[]{"%"+s+"%","%"+s+"%"},//查询语句对应的问号中的内容
                        null,
                        null,
                        NotePad.Notes.DEFAULT_SORT_ORDER);
                int[] viewIDs = { R.id.text3,R.id.text4};
                // Creates the backing adapter for the ListView.
                SimpleCursorAdapter adapter
                        = new SimpleCursorAdapter(//把查询结果放入adapter中
                        NoteSearch.this,                             // The Context for the ListView
                        R.layout.notesearch_listview,          // Points to the XML for a list item
                        cursor,                           // The cursor to get items from
                        new String[]{NotePad.Notes.COLUMN_NAME_TITLE,NotePad.Notes.COLUMN_NAME_CREATE_DATE},
                        viewIDs
                );

                // Sets the ListView's adapter to be the cursor adapter that was just created.
                listview.setAdapter(adapter);//listview用于显示查询的结果实现实时搜索
                return true;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {//对listview的item添加监听事件，可以点击进入查看对应的便签内容
                // Constructs a new URI from the incoming URI and the row ID
                Uri uri = ContentUris.withAppendedId(getIntent().getData(), l);
                // Gets the action from the incoming Intent
                String action = getIntent().getAction();
                // Handles requests for note data
                if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
                    // Sets the result to return to the component that called this Activity. The
                    // result contains the new URI
                    setResult(RESULT_OK, new Intent().setData(uri));
                } else {
                    // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
                    // Intent's data is the note ID URI. The effect is to call NoteEdit.
                    startActivity(new Intent(Intent.ACTION_EDIT, uri));
                }
            }
        });
    }
}

~~~
#### 4.NoteSearch.java布局文件note_search.xml
~~~
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <!--searchView搜索框-->
    <SearchView   
        android:id="@+id/search"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:iconifiedByDefault="false"
        android:queryHint="搜索"></SearchView>
    <!--listview用于存放搜索结果的列表视图-->
    <ListView
        android:id="@+id/listview"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"></ListView>
</LinearLayout>
~~~



