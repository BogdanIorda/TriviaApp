package com.example.triviaapp.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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


    if (viewModel.data.value.loading == true) {

        CircularProgressIndicator()

    } else {
        val question = try {
            questions?.get(questionIndex.intValue)
        } catch (e: Exception) {
            null
        }

        if (questions != null) {
            QuestionDisplay(
                question = question!!,
                questionIndex = questionIndex,
                viewModel = viewModel
            ) {
                questionIndex.intValue++
            }
        }
    }
}

// TODO: We have a bug , if the answer is to long, we can't see the next line
@Composable
fun QuestionDisplay(

    question: QuestionItem,
    questionIndex: MutableState<Int>,
    viewModel: QuestionsViewModel,
    onNextClicked: (Int) -> Unit = {}
) {

    val choicesState = remember(question) { question.choices.toMutableList() }
    var answerState by remember(question) { mutableStateOf<Int?>(null) }
    var correctAnswerState by remember(question) { mutableStateOf<Boolean?>(null) }

    val updateAnswer: (Int) -> Unit = remember(question) {
        { index ->
            answerState = index
            correctAnswerState = choicesState[index] == question.answer
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

            ShowProgress(questionIndex.value)


            QuestionTracker(questionIndex.value + 1, viewModel.getTotalQuestionCount())
            DrawDottedLine(pathStyle)
            Column() {
                Text(
                    text = question.question,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Start)
                        .fillMaxHeight(0.3f),
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
                            .height(50.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Transparent)
                            .border(
                                width = 4.dp, brush = Brush.linearGradient(
                                    colors = listOf(
                                        AppColors.myOffDarkPurple,
                                        AppColors.myOffDarkPurple
                                    )
                                ),
                                shape = RoundedCornerShape(25.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (answerState == index),
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
                                            true if index == answerState -> {
                                                Color.Green
                                            }

                                            false if index == answerState -> {
                                                Color.Red
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
                                .padding(8.dp)
                        )
                    }
                }
                Button(
                    onClick = { onNextClicked(questionIndex.value) },
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .align(alignment = Alignment.CenterHorizontally),

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

//TODO:Do the exercises at the end of this video: https://www.udemy.com/course/kotling-android-jetpack-compose-/learn/lecture/29429748#content

//TODO: After you do the TODO from above , implement other functionalities to the app. There are a lot.
@Composable
fun DrawDottedLine(pathEffect: PathEffect) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp),
        {
            drawLine(
                color = AppColors.myLightGray,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                pathEffect = pathEffect
            )
        }
    )
}


@Composable
fun ShowProgress(score: Int) {

    val gradient = Brush.linearGradient(
        listOf(
            Color(0xFFF95075),
            Color(0xFFBE6BE5)
        )
    )

    val progressFactor by remember(score) {
        mutableFloatStateOf(score * 0.005f)
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

        Text(
            text = (score).toString(),
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = AppColors.myOffWhite
        )
    }
}

























