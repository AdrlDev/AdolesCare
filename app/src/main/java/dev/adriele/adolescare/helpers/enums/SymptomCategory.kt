package dev.adriele.adolescare.helpers.enums

import androidx.annotation.StringRes
import dev.adriele.language.R

enum class SymptomCategory(@StringRes val labelRes: Int, val options: List<SymptomOption>) {
    SEX_DRIVE(
        R.string.sex_drive, listOf(
        SymptomOption.NO_SEX,
        SymptomOption.PROTECTED_SEX,
        SymptomOption.UNPROTECTED_SEX,
        SymptomOption.ORAL_SEX,
        SymptomOption.ANAL_SEX,
        SymptomOption.MASTURBATION,
        SymptomOption.SENSUAL_TOUCH,
        SymptomOption.SEX_TOYS,
        SymptomOption.ORGASM,
        SymptomOption.HIGH_SEX_DRIVE,
        SymptomOption.NEUTRAL_SEX_DRIVE,
        SymptomOption.LOW_SEX_DRIVE
    )),
    MOOD(R.string.mood, listOf(
        SymptomOption.CALM,
        SymptomOption.HAPPY,
        SymptomOption.ENERGETIC,
        SymptomOption.FRISKY,
        SymptomOption.MOOD_SWINGS,
        SymptomOption.IRRITATED,
        SymptomOption.SAD,
        SymptomOption.ANXIOUS,
        SymptomOption.DEPRESSED,
        SymptomOption.FEELING_GUILTY,
        SymptomOption.OBSESSIVE_THOUGHTS,
        SymptomOption.LOW_ENERGY,
        SymptomOption.APATHETIC,
        SymptomOption.CONFUSED,
        SymptomOption.VERY_SELF_CRITICAL
    )),
    SYMPTOMS(R.string.symptoms_category, listOf(
        SymptomOption.EVERYTHING_FINE,
        SymptomOption.CRAMP,
        SymptomOption.HEADACHE,
        SymptomOption.TENDER_BREAST,
        SymptomOption.BACKACHE,
        SymptomOption.ACNE,
        SymptomOption.FATIGUE,
        SymptomOption.CRAVINGS,
        SymptomOption.INSOMNIA,
        SymptomOption.ABDOMINAL_PAIN,
        SymptomOption.VAGINAL_ITCHING,
        SymptomOption.VAGINAL_DRYNESS
    )),
    VAGINAL_DISCHARGE(R.string.vaginal_discharge, listOf(
        SymptomOption.NO_DISCHARGE,
        SymptomOption.CREAMY,
        SymptomOption.WATERY,
        SymptomOption.STICKY,
        SymptomOption.EGG_WHITE,
        SymptomOption.SPOTTING,
        SymptomOption.UNUSUAL,
        SymptomOption.CLUMPY_WHITE,
        SymptomOption.GRAY
    )),
    DIGESTION_AND_STOOL(R.string.digestion_and_stool, listOf(
        SymptomOption.NAUSEA,
        SymptomOption.BLOATING,
        SymptomOption.DIARRHEA,
        SymptomOption.CONSTIPATION
    )),
    PREGNANCY_TEST(R.string.pregnancy_test, listOf(
        SymptomOption.DIDNT_TAKE_TEST,
        SymptomOption.POSITIVE,
        SymptomOption.NEGATIVE,
        SymptomOption.FAINT_LINE
    )),
    PHYSICAL_ACTIVITY(R.string.physical_activity, listOf(
        SymptomOption.DIDNT_EXERCISE,
        SymptomOption.YOGA,
        SymptomOption.GYM,
        SymptomOption.AEROBICS_DANCING,
        SymptomOption.SWIMMING,
        SymptomOption.RUNNING,
        SymptomOption.CYCLING,
        SymptomOption.WALKING
    ));
}