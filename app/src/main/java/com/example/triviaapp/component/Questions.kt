package com.example.triviaapp.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.triviaapp.model.QuestionItem
import com.example.triviaapp.screens.QuestionsViewModel
import com.example.triviaapp.util.AppColors

@Composable
fun Questions(viewModel: QuestionsViewModel) {

    val questions = viewModel.data.value.data?.toMutableList()
    val questionIndex = remember(questions) { mutableIntStateOf(0) }
    val score = remember { mutableIntStateOf(0) }
    val isGameOver = remember { mutableStateOf(false) }
    val gameAttempt = remember { mutableIntStateOf(1) }
    val streakCount = remember { mutableIntStateOf(0) }

    if (viewModel.data.value.loading == true) {

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

    } else {
        val question = try {
            questions?.get(questionIndex.intValue)
        } catch (e: Exception) {
            null
        }
        if (isGameOver.value) {
            GameOver(
                score.intValue,
                onNewGameClicked = {
                    score.intValue = 0
                    questionIndex.intValue = 0
                    viewModel.clearMemoryBank()
                    isGameOver.value = false
                    gameAttempt.intValue++ // just in case we change the GameOver composable
                    streakCount.intValue = 0
                }
            )
        } else {
            if (questions != null) {
                QuestionDisplay(
                    score = score,
                    question = question!!,
                    questionIndex = questionIndex,
                    viewModel = viewModel,
                    gameAttempt = gameAttempt,
                    streakCount = streakCount,
                    onNextClicked = {
                        if (questionIndex.intValue < viewModel.getTotalQuestionCount() - 1) {
                            questionIndex.intValue++
                        } else {
                            isGameOver.value = true
                            //TODO: Ask ai how to make so the app wont allow screen turn
                        }

                    },
                    onBackClicked = {
                        if (questionIndex.intValue > 0) {
                            questionIndex.intValue--
                        }
                    },
                    onNewGameClicked = {
                        score.intValue = 0
                        questionIndex.intValue = 0
                        viewModel.clearMemoryBank()
                        streakCount.intValue = 0
                        gameAttempt.intValue++
                    }
                )
            }
        }
    }
}

@Composable
fun QuestionDisplay(
    score: MutableState<Int>,
    question: QuestionItem,
    questionIndex: MutableState<Int>,
    viewModel: QuestionsViewModel,
    gameAttempt: MutableState<Int>,
    streakCount: MutableState<Int>,
    onNextClicked: (Int) -> Unit = {},
    onBackClicked: (Int) -> Unit = {},
    onNewGameClicked: () -> Unit = {}
) {

    val context = LocalContext.current

    val newGamePopUpShowing = remember { mutableStateOf(false) }

    val choicesState = remember(question) { question.choices.toMutableList() }

    var selectedAnswerIndexState by remember(question, gameAttempt.value) {
        mutableStateOf(
            viewModel.getAnswerFromHistory(
                questionIndex.value
            )
        )
    }

    var correctAnswerState by remember(question, gameAttempt.value) {
        mutableStateOf(
            if (selectedAnswerIndexState != null) {
                choicesState[selectedAnswerIndexState!!] == question.answer
            } else null
        )
    }

    val updateAnswer: (Int) -> Unit = remember(question, gameAttempt.value) {
        { tappedAnswerIndex ->
            if (selectedAnswerIndexState == null) {
                selectedAnswerIndexState = tappedAnswerIndex
                viewModel.saveAnswerToHistory(questionIndex.value, tappedAnswerIndex)
                correctAnswerState = choicesState[tappedAnswerIndex] == question.answer

                if (correctAnswerState == true) {
                    streakCount.value += 1
                    when (streakCount.value) {
                        in 4..6 -> score.value += 2
                        in 7..10 -> score.value += 3
                        in 11..viewModel.getTotalQuestionCount() -> score.value += 5
                        else -> score.value += 1
                    }

                } else {
                    streakCount.value = 0
                    if (score.value > 0) {
                        score.value -= 1
                    }
                }
            }
        }
    }

    val pathStyle = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = AppColors.myDarkPurple
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            ShowProgress(score.value, streakCount = streakCount, viewModel.getTotalQuestionCount())

            QuestionTracker(questionIndex.value + 1, viewModel.getTotalQuestionCount())
            DrawDottedLine(pathStyle)
            Column() {
                Text(
                    text = question.question,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.Start),
                    color = AppColors.myOffWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
                //choices
                choicesState.forEachIndexed { index, answerText ->
                    Row(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 50.dp)
                            .clip(RoundedCornerShape(size = 50.dp))
                            .background(Color.Transparent)
                            .border(
                                width = 4.dp, brush = Brush.linearGradient(
                                    colors = listOf(
                                        AppColors.myOffDarkPurple,
                                        AppColors.myOffDarkPurple
                                    )
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedAnswerIndexState == index),
                            onClick = {
                                updateAnswer(index)
                            },
                            modifier = Modifier
                                .padding(start = 16.dp),
                            colors = RadioButtonDefaults.colors(
                                selectedColor =
                                    if (correctAnswerState == true) {
                                        Color.Green.copy(0.2f)
                                    } else {
                                        Color.Red.copy(0.2f)
                                    }
                            )
                        )
                        val annotatedString = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Light,
                                    color =
                                        when (correctAnswerState) {
                                            true if index == selectedAnswerIndexState -> {
                                                Color.Green
                                            }

                                            false if index == selectedAnswerIndexState -> {
                                                Color.Red
                                            }

                                            false if answerText == question.answer && index != selectedAnswerIndexState -> {
                                                Color.Green
                                            }

                                            else -> {
                                                AppColors.myOffWhite
                                            }
                                        },
                                    fontSize = 18.sp
                                )
                            ) {
                                append(answerText)
                            }
                        }
                        Text(
                            text = annotatedString,
                            modifier = Modifier
                                .padding(12.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onBackClicked(questionIndex.value) },
                        modifier = Modifier
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(33.dp),
                        colors = ButtonDefaults.buttonColors(AppColors.myLightBlue),
                        enabled = questionIndex.value > 0
                    )
                    {
                        Text(
                            text = "Back",
                            modifier = Modifier
                                .padding(4.dp),
                            color = AppColors.myOffWhite,
                            fontSize = 18.sp
                        )
                    }
                    Button(
                        onClick = { onNextClicked(questionIndex.value) },
                        modifier = Modifier
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(33.dp),
                        colors = ButtonDefaults.buttonColors(AppColors.myLightBlue)
                    )
                    {
                        Text(
                            text = "Next",
                            modifier = Modifier
                                .padding(4.dp),
                            color = AppColors.myOffWhite,
                            fontSize = 18.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            newGamePopUpShowing.value = true
                        },
                        modifier = Modifier
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(33.dp),
                        colors = ButtonDefaults.buttonColors(AppColors.myBlue)
                    ) {
                        Text(
                            text = "NewGame",
                            modifier = Modifier
                                .padding(4.dp),
                            color = AppColors.myOffWhite
                        )
                    }

                    if (newGamePopUpShowing.value) {
                        AlertDialog(
                            onDismissRequest = { newGamePopUpShowing.value = false },
                            title = { Text(text = "Are you sure?") },
                            text = { Text(text = "All your progress will be lost.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        onNewGameClicked()
                                        Toast.makeText(
                                            context,
                                            "Welcome to a New Game",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        newGamePopUpShowing.value = false
                                    }
                                ) { Text("Yes") }
                            },
                            dismissButton = {
                                Button(onClick = {
                                    newGamePopUpShowing.value = false
                                }) { Text("No") }
                            }

                        )
                    }
                }
            }
        }
    }
}


@Composable
fun QuestionTracker(counter: Int, outOf: Int) {
    Text(
        modifier = Modifier
            .padding(20.dp),
        text = buildAnnotatedString {
            withStyle(style = ParagraphStyle(textIndent = TextIndent.None)) {
                withStyle(
                    style = SpanStyle(
                        color = AppColors.myLightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                ) {
                    append("Question: $counter/")
                }
                withStyle(
                    style = SpanStyle(
                        color = AppColors.myLightGray,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp
                    )
                ) {
                    append("$outOf")
                }
            }
        }
    )
}


@Composable
fun DrawDottedLine(pathEffect: PathEffect) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = AppColors.myLightGray,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect
        )
    }
}


@Composable
fun ShowProgress(score: Int, streakCount: MutableState<Int>, outOf: Int) {

    val flame = when (streakCount.value) {
        in 3..6 -> "🔥"
        in 7..10 -> "🔥🔥"
        in 11..<outOf -> "🔥🔥🔥"
        else -> ""
    }
    val gradient = Brush.linearGradient(
        listOf(
            Color(0xFFF95075),
            Color(0xFFBE6BE5)
        )
    )

    val progressFactor by remember(score) {
        mutableFloatStateOf(score * 0.01f)
    }

    Box(
        modifier = Modifier
            .padding(top = 40.dp)
            .fillMaxWidth()
            .height(50.dp)
            .border(
                4.dp, brush = Brush.linearGradient(
                    colors = listOf(
                        AppColors.myLightPurple,
                        AppColors.myLightPurple
                    )
                ),
                shape = RoundedCornerShape(33.dp)
            )
            .clip(RoundedCornerShape(50.dp))
            .background(Color.Transparent),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progressFactor)
                .fillMaxHeight()
                .clip(
                    RoundedCornerShape(
                        topStart = 33.dp,
                        bottomStart = 33.dp,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .background(brush = gradient)
        )

        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        )
        {
            Text(
                text = "Score: $score",
                color = AppColors.myOffWhite
            )

            Text(
                text = "Streak: ${streakCount.value}$flame",
                color = AppColors.myOffWhite
            )
        }
    }
}

@Composable
fun GameOver(score: Int, onNewGameClicked: () -> Unit = {}) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Game Over",
                fontSize = 40.sp
            )
            Text(
                text = "You final score is: $score",
                fontSize = 20.sp
            )

            Button(
                onClick = {
                    onNewGameClicked()
                    Toast.makeText(
                        context,
                        "Welcome to a New Game",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(33.dp),
                colors = ButtonDefaults.buttonColors(AppColors.myBlue)
            ) {
                Text(
                    text = "NewGame",
                    modifier = Modifier
                        .padding(4.dp),
                    color = AppColors.myOffWhite
                )
            }
        }
    }
}























