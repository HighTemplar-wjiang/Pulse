#define B1  0b00000001
#define LED 0b01000000
#define INT 100
#define BITS_ONCE 1
#define CHANNELX 0
#define CHANNELY 0
#define CHANNELZ 1
#define SEGMENT_LENGTH 2 // SEGMENT_LENGTH = CHANNELS * BITS_ONCE, MUST CHANGE IT IF OTHER VALUE CHANGED

#include <avr/io.h>
#include <util/delay.h>
#include <string.h>

void zeros();
void sendSegment(const char bits[SEGMENT_LENGTH]);
void sendString(const char *str);

int main(void)
{
    DDRA  |= 1 << PINA6; // LED 
	DDRA  &= ~(1 << PINA0); // button 1	
	DDRA  &= ~(1 << PINA1); // button 2
	DDRA  &= ~(1 << PINA2); // button 3
	DDRA  &= ~(1 << PINA3); // button 4
	DDRA  &= ~(1 << PINA4); // button 5
	
	// magnetism X
	DDRB |= 1 << PINB7; // output: EXT2 12
	DDRB |= 1 << PINB6; // output: EXT2 11
	DDRB |= 1 << PINB5; // output: EXT2 10
	DDRB |= 1 << PINB4; // output: EXT2 9
	
	// magnetism Y
	DDRD |= 1 << PIND7; // output: EXT1 14
	DDRD |= 1 << PIND6; // output: EXT1 13
	DDRD |= 1 << PIND5; // output: EXT1 12
	DDRD |= 1 << PIND4; // output: EXT1 11
	
	// magntism Z
	DDRE |= 1 << PINE7; // output: EXT1 8
	DDRE |= 1 << PINE6; // output: EXT1 7
	DDRE |= 1 << PINE5; // output: EXT1 6
	DDRE |= 1 << PINE4; // output: EXT1 5

	zeros();
	
	char string[] = "Hello world! We are from Univerty of Oulu, Finland.\n";
	
	//_delay_ms(5000);	
	while(1)
	{
	    if (bit_is_clear(PINA, 0))
		{
			PORTA |= 1 << PINA6; // LED on
			startFlag();
			sendString(string1);
			endFlag();
			PORTA &= ~(1 << PINA6); // LED off
		}
		
		if (bit_is_clear(PINA, 1))
		{
		    PORTA |= 1 << PINA6; // LED on
			startFlag();
			sendString(string2);
			endFlag();
			PORTA &= ~(1 << PINA6); // LED off
		}
		
		if (bit_is_clear(PINA, 2))
		{
		    PORTA |= 1 << PINA6; // LED on
			startFlag();
			sendString(string3);
			endFlag();
			PORTA &= ~(1 << PINA6); // LED off
		}
		
		if (bit_is_clear(PINA, 3))
		{
		    PORTA |= 1 << PINA6; // LED on
			startFlag();
			sendString(string4);
			endFlag();
			PORTA &= ~(1 << PINA6); // LED off
		}
		
		if (bit_is_clear(PINA, 4))
		{
		    PORTA |= 1 << PINA6; // LED on
			PORTA &= ~(1 << PINA7);
			PORTC |= 1 << PINC3;
			_delay_ms(2000);
			PORTA &= ~(1 << PINA6); // LED off
		}
	}
}

// set all output to zero
void zeros()
{
    PORTB &= ~(1 << PINB7);
	PORTB &= ~(1 << PINB6);
	PORTB &= ~(1 << PINB5);
	PORTB &= ~(1 << PINB4);
	
	PORTD &= ~(1 << PIND7);
	PORTD &= ~(1 << PIND6);
	PORTD &= ~(1 << PIND5);
	PORTD &= ~(1 << PIND4);
	
	PORTE &= ~(1 << PINE7);
	PORTE &= ~(1 << PINE6);
	PORTE &= ~(1 << PINE5);
	PORTE &= ~(1 << PINE4);
}

void sendSegment(const char bits[SEGMENT_LENGTH])
{
	int iPosition = 0;
	int iCount = BITS_ONCE;
	if(CHANNELX == 1)
	{
		if(iCount-- > 0) PORTB |= bits[iPosition++] << PINB4;
		if(iCount-- > 0) PORTB |= bits[iPosition++] << PINB5;
		if(iCount-- > 0) PORTB |= bits[iPosition++] << PINB6;
		if(iCount-- > 0) PORTB |= bits[iPosition++] << PINB7;
	}
	
	iCount = BITS_ONCE;
	if(CHANNELY == 1)
	{
		if(iCount-- > 0) PORTD |= bits[iPosition++] << PIND4;
		if(iCount-- > 0) PORTD |= bits[iPosition++] << PIND5;
		if(iCount-- > 0) PORTD |= bits[iPosition++] << PIND6;
		if(iCount-- > 0) PORTD |= bits[iPosition++] << PIND7;
	}
	
	iCount = BITS_ONCE;
	if(CHANNELZ == 1)
	{
		if(iCount-- > 0) PORTE |= bits[iPosition++] << PINE4;
		if(iCount-- > 0) PORTE |= bits[iPosition++] << PINE5;
		if(iCount-- > 0) PORTE |= bits[iPosition++] << PINE6;
		if(iCount-- > 0) PORTE |= bits[iPosition++] << PINE7;
	}
}

void sendString(const char *str)
{
    char segment_buffer[SEGMENT_LENGTH];
	char bits_buffer[SEGMENT_LENGTH * 8];
    
	int length = strlen(str);
	int add_zeros = SEGMENT_LENGTH - (length % SEGMENT_LENGTH);
	int iSentCount = 0;
	
	while(iSentCount < length)
	{
	    int i = 0; // get SEGMENT_LENGTH characters once
		int j = 0;
		char current = '\0';
	    while(i < SEGMENT_LENGTH)
		{
		    if(str[iSentCount] != '\0')
			{
			    current = str[iSentCount++];
			}
			else
			{
			    current = '\0';
			}
			
			for(j = 0; j < 8; j++)
			{
			    bits_buffer[i * 8 + j] = (current >> j) & 0x01;
			}
		    i++;
		}
		for(i = 0; i < 8; i++)
		{
		    for(j = 0; j < SEGMENT_LENGTH; j++)
			{
			    segment_buffer[j] = bits_buffer[i * 8 + j];
				sendSegment(segment_buffer);
			}
		}		
	}
}
