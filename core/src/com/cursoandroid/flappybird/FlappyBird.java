package com.cursoandroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.lang.reflect.Field;
import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private BitmapFont fonte;
	private BitmapFont mensagem;
	private Circle passaroCirculo;
	private Rectangle retanguloCanoTopo;
	private Rectangle retanguloCanoBaixo;
	//private ShapeRenderer shape;

	//atributos de configurações e movimentos
	private float larguraDispositivo;
	private float alturaDispositivo;
	private Random numeroRandomico;
	private int estadoJogo = 0;
	private int pontuacao = 0;

	private float variacao = 0;
	private float velocidadeQueda = 0;
	private float posicaoInicialVertical;
	private  float posicaoMovimentoCanoHorizontal;
	private  float espacoCanos;
	private float deltatime;
	private  float alturaCanosRandomica;
	private  Boolean marcouPonto = false;

	//camera
    private OrthographicCamera camera;
    private Viewport viewPort;
    private final float VIRTUAL_WIDTH = 768;
    private final float VIRTUAL_HEIGHT = 1024;
	
	@Override
	public void create () {
		batch = new SpriteBatch();

		numeroRandomico = new Random();

		passaroCirculo = new Circle();
		//retanguloCanoBaixo = new Rectangle();
		//retanguloCanoTopo = new Rectangle();
		//shape = new ShapeRenderer();

		fonte = new BitmapFont();
		fonte.setColor(Color.WHITE);
		fonte.getData().setScale(6);

		mensagem = new BitmapFont();
		mensagem.setColor(Color.WHITE);
		mensagem.getData().setScale(3);

		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo.png");
		canoTopo = new Texture("cano_topo.png");
		gameOver = new Texture( "game_over.png" );

		//configuração de camera

        camera = new OrthographicCamera();
        camera.position.set( VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0 );
        viewPort = new StretchViewport( VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera );

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;

		posicaoInicialVertical = alturaDispositivo / 2;
		posicaoMovimentoCanoHorizontal = larguraDispositivo;
		espacoCanos = 200;
	}

	@Override
	public void render () {

		camera.update();

		//limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

		//passaro fica batendo as assas
		deltatime = Gdx.graphics.getDeltaTime();
		variacao += Gdx.graphics.getDeltaTime() * 5;

		if (variacao > 2) variacao = 0;

		if ( estadoJogo == 0 ){// 0= jogo não iniciado; 1= jogo iniciado; 2= game over;
			if ( Gdx.input.justTouched() ){
				estadoJogo = 1;
			}
		}else if ( estadoJogo == 1 ){ //jogo iniciado

            velocidadeQueda++;
            if (posicaoInicialVertical > 0 || velocidadeQueda < 0)
                posicaoInicialVertical = posicaoInicialVertical - velocidadeQueda;

		    if ( estadoJogo == 1 ){

                //velocidade do cano

                posicaoMovimentoCanoHorizontal -= deltatime * 300;

                if (Gdx.input.justTouched()) {
                    velocidadeQueda = -10;
                }

                //verifica se o cano saiu da tela totalmente
                if (posicaoMovimentoCanoHorizontal < -canoTopo.getHeight()) {
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
					alturaCanosRandomica = numeroRandomico.nextInt(400) - 200;
                    marcouPonto = false;
                }

                //verifica pontuacao
                if ( posicaoMovimentoCanoHorizontal < 120 ){
                    if ( !marcouPonto ){
                        pontuacao ++;
                        marcouPonto = true;
                    }
                }

            }
		}else if ( estadoJogo == 2 ){ //tela de game over estado de game 2

			if ( Gdx.input.justTouched() ){

				estadoJogo = 0;
				pontuacao = 0;
				velocidadeQueda = 0;
				posicaoInicialVertical = alturaDispositivo / 2;
				posicaoMovimentoCanoHorizontal = larguraDispositivo;
			}

        }

		//configurar projeção da camera
        batch.setProjectionMatrix( camera.combined );

		batch.begin();

		batch.draw( fundo, 0, 0, larguraDispositivo, alturaDispositivo );
		batch.draw( canoTopo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoCanos / 2 + alturaCanosRandomica);
		batch.draw( canoBaixo, posicaoMovimentoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoCanos / 2  + alturaCanosRandomica);
		batch.draw( passaros[ (int) variacao ], 120, posicaoInicialVertical );
		fonte.draw( batch, String.valueOf(pontuacao), larguraDispositivo / 2, alturaDispositivo - 50 );

		if ( estadoJogo == 2 ){ //tela de game over
			batch.draw( gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2 );
			mensagem.draw( batch, "Toque para reiniciar o game",larguraDispositivo / 2 - 270,
					alturaDispositivo / 2 - gameOver.getHeight() / 2 );
		}

		batch.end();

		//desenhar o passaro e canos
		passaroCirculo.set( 120 + passaros[0].getWidth() / 2, posicaoInicialVertical + passaros[0].getHeight() / 2,
				passaros[0].getWidth() / 2 );

		retanguloCanoBaixo = new Rectangle(
			posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight()
				- espacoCanos / 2  + alturaCanosRandomica, canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		retanguloCanoTopo = new Rectangle(
			posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoCanos / 2 + alturaCanosRandomica,
				canoTopo.getWidth(), canoTopo.getHeight()
		);


		//desenhar formas
		/*
		shape.begin( ShapeRenderer.ShapeType.Filled );

		shape.circle( passaroCirculo.x, passaroCirculo.y, passaroCirculo.radius );
		shape.rect( retanguloCanoBaixo.x, retanguloCanoBaixo.y, retanguloCanoBaixo.width, retanguloCanoBaixo.height );
		shape.rect( retanguloCanoTopo.x, retanguloCanoTopo.y, retanguloCanoTopo.width, retanguloCanoTopo.height );
		shape.setColor(Color.RED);

		shape.end();
		 */

		//teste de colisoes
		if ( Intersector.overlaps( passaroCirculo, retanguloCanoBaixo )
				|| Intersector.overlaps( passaroCirculo, retanguloCanoTopo ) || posicaoInicialVertical <= 0
				|| posicaoInicialVertical >= alturaDispositivo ){

			estadoJogo = 2;

		}

	}

    @Override
    public void resize(int width, int height) {
        viewPort.update( width, height );
    }
}
