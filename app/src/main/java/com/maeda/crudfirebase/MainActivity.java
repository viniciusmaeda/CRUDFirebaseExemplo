package com.maeda.crudfirebase;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.maeda.crudfirebase.modelo.Pessoa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // objetos correspondentes aos componentes da Activity
    private EditText campoNome;
    private EditText campoEmail;
    private ListView ltvDados;

    // objetos utilizados para conectar ao Firebase (BD)
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    // define objeto com os dados do contato
    private Pessoa pessoaSelecionada;

    // objetos para manter a lista de registros do BD
    private List<Pessoa> listPessoa = new ArrayList<Pessoa>();
    private ArrayAdapter<Pessoa> arrayAdapterPessoa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // relacionar os objetos com os componentes
        campoNome = (EditText) findViewById(R.id.edt_nome);
        campoEmail = (EditText) findViewById(R.id.edt_email);
        ltvDados = (ListView) findViewById(R.id.ltv_dados);

        // chama o método para iniciar o Firebase (BD)
        inicializarFirebase();

        // método para criar um evento (Listener)
        eventoDataBase();

        // implementa um listener para capturar o item selecionado no ListView
        ltvDados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // transforma o item selecionado num objeto do tipo Pessoa
                pessoaSelecionada = (Pessoa) adapterView.getItemAtPosition(i);
                // atribui os valores às propriedades do objeto
                campoNome.setText(pessoaSelecionada.getNome());
                campoEmail.setText(pessoaSelecionada.getEmail());
            }
        });

    }

    // método que implementa um listener que fica verificando se houve alteração no BD.
    // havendo alteração, é realizado uma atualização no ListView
    private void eventoDataBase() {
        databaseReference.child("Pessoa").addValueEventListener(new ValueEventListener() {
            // sobrescreve método que verifica se houve mudança no banco
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listPessoa.clear(); // limpa o ListView antes de popular novamente
                for (DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
                    Pessoa p = objSnapshot.getValue(Pessoa.class);
                    listPessoa.add(p);
                }
                arrayAdapterPessoa = new ArrayAdapter<Pessoa>(MainActivity.this, android.R.layout.simple_list_item_1, listPessoa);
                ltvDados.setAdapter(arrayAdapterPessoa);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // método responsável para criar uma instância com o Firebase
    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference();
    }


    // método que cria o menu na barra superior
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // obtém o
        int id = item.getItemId();

        if (id == R.id.menu_novo) { // opção para cadastrar um novo registro
            // cria um objeto do tipo pessoa
            Pessoa p = new Pessoa();
            // gera um ID para o novo registro
            p.setUid(UUID.randomUUID().toString());
            // captura o valor do campo e armazena na propriedade nome do objeto
            p.setNome(campoNome.getText().toString());
            // captura o valor do campo e armazena na propriedade nome do objeto
            p.setEmail(campoEmail.getText().toString());
            // salva os dados do objeto no BD usando como nome de tabela "Pessoa"
            databaseReference.child("Pessoa").child(p.getUid()).setValue(p);
            // ao final, chama método para limpar os campos
            limparCampos();

        } else if (id == R.id.menu_atualiza) { // opção para atualizar um registro existente
            // cria um objeto do tipo pessoa
            Pessoa p = new Pessoa();
            // obtém o ID do objeto selecionado no ListView
            p.setUid(pessoaSelecionada.getUid());
            // captura o valor do campo e armazena na propriedade nome do objeto
            p.setNome(campoNome.getText().toString().trim());
            // captura o valor do campo e armazena na propriedade nome do objeto
            p.setEmail(campoEmail.getText().toString().trim());
            // salva os dados do objeto no BD usando como nome de tabela "Pessoa"
            databaseReference.child("Pessoa").child(p.getUid()).setValue(p);
            // ao final, chama método para limpar os campos
            limparCampos();

        } else if (id == R.id.menu_deleta) { // opção para excluir um registro existente
            // cria um objeto do tipo pessoa
            Pessoa p = new Pessoa();
            // obtém o ID do objeto selecionado no ListView
            p.setUid(pessoaSelecionada.getUid());
            // excluir os dados do registro a partir do ID
            databaseReference.child("Pessoa").child(p.getUid()).removeValue();
            // ao final, chama método para limpar os campos
            limparCampos();
        }

        return true;
    }

    // método que irá limpar os campos (EditText) do formulário
    private void limparCampos() {
        campoNome.setText("");
        campoEmail.setText("");
        campoNome.requestFocus(); // mantém o focus neste campo após limpar
    }
}
